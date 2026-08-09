#!/usr/bin/env python3
"""Review a pull request with Gemini and an OpenRouter fallback.

The workflow checks out the trusted base revision before running this file. Pull
request metadata and patches are fetched through the GitHub API and are treated
as untrusted text; pull request code is never executed in the secret-bearing job.
"""

from __future__ import annotations

import hashlib
import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Callable, TypeVar

import review_publishing

COMMENT_MARKER = "<!-- whylog-ai-review -->"
GEMINI_MODEL = "gemini-3.6-flash"
OPENROUTER_MODEL = "poolside/laguna-s-2.1:free"
SYSTEM_PROMPT_PATH = Path(".github/prompts/ai-review.md")

MAX_PR_BODY_CHARS = 10_000
MAX_CONTEXT_FILE_CHARS = 20_000
MAX_CONTEXT_CHARS = 90_000
MAX_PATCH_CHARS = 30_000
MAX_DIFF_CHARS = 170_000
MAX_FINDINGS_PER_KIND = 20
MAX_OUTPUT_TOKENS = 8_192
REQUEST_TIMEOUT_SECONDS = 60
RETRY_DELAYS_SECONDS = (1, 2, 4)
TRANSIENT_HTTP_STATUSES = {408, 409, 425, 429}

T = TypeVar("T")


class ReviewError(RuntimeError):
    """Raised when an AI review cannot be produced safely."""


class HttpRequestError(ReviewError):
    def __init__(self, status: int, reason: str = "") -> None:
        self.status = status
        message = f"HTTP {status}"
        if reason:
            message += f" ({reason})"
        super().__init__(message)


class NetworkRequestError(ReviewError):
    """Raised for retryable transport failures."""


@dataclass(frozen=True)
class Finding:
    title: str
    file: str
    line: int | None
    reason: str
    rule_reference: str
    recommendation: str


@dataclass(frozen=True)
class Review:
    summary: str
    blocking: tuple[Finding, ...]
    suggestions: tuple[Finding, ...]


@dataclass(frozen=True)
class ProviderResult:
    provider: str
    model: str
    review: Review
    fallback_reason: str | None = None


def request_json(
    url: str,
    *,
    method: str = "GET",
    headers: dict[str, str] | None = None,
    payload: Any | None = None,
    timeout: int = REQUEST_TIMEOUT_SECONDS,
) -> Any:
    request_headers = {
        "Accept": "application/json",
        "User-Agent": "WhyLog-AI-Review/1.0",
        **(headers or {}),
    }
    data = None
    if payload is not None:
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        request_headers.setdefault("Content-Type", "application/json")

    request = urllib.request.Request(
        url,
        data=data,
        headers=request_headers,
        method=method,
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            body = response.read()
            if not body:
                return None
            try:
                return json.loads(body)
            except json.JSONDecodeError as error:
                raise ReviewError("HTTP response was not valid JSON") from error
    except urllib.error.HTTPError as error:
        raise HttpRequestError(error.code, error.reason) from error
    except (urllib.error.URLError, TimeoutError, OSError) as error:
        raise NetworkRequestError(type(error).__name__) from error


def is_transient(error: Exception) -> bool:
    return isinstance(error, NetworkRequestError) or (
        isinstance(error, HttpRequestError)
        and (error.status in TRANSIENT_HTTP_STATUSES or error.status >= 500)
    )


def with_retry(operation: Callable[[], T]) -> T:
    attempts = len(RETRY_DELAYS_SECONDS) + 1
    for attempt in range(attempts):
        try:
            return operation()
        except (HttpRequestError, NetworkRequestError) as error:
            if not is_transient(error) or attempt == attempts - 1:
                raise
            time.sleep(RETRY_DELAYS_SECONDS[attempt])
    raise AssertionError("retry loop ended unexpectedly")


def _extract_gemini_text(response: Any) -> str:
    try:
        parts = response["candidates"][0]["content"]["parts"]
        text = "".join(part.get("text", "") for part in parts)
    except (KeyError, IndexError, TypeError) as error:
        raise ReviewError("Gemini response did not contain review text") from error
    if not text.strip():
        raise ReviewError("Gemini returned an empty review")
    return text


def call_gemini(api_key: str, system_prompt: str, user_prompt: str) -> str:
    model = urllib.parse.quote(GEMINI_MODEL, safe="")
    url = (
        "https://generativelanguage.googleapis.com/v1beta/models/"
        f"{model}:generateContent"
    )
    response = request_json(
        url,
        method="POST",
        headers={"x-goog-api-key": api_key},
        payload={
            "systemInstruction": {"parts": [{"text": system_prompt}]},
            "contents": [{"role": "user", "parts": [{"text": user_prompt}]}],
            "generationConfig": {
                "maxOutputTokens": MAX_OUTPUT_TOKENS,
                "responseMimeType": "application/json",
            },
        },
    )
    return _extract_gemini_text(response)


def _extract_openrouter_text(response: Any) -> str:
    try:
        content = response["choices"][0]["message"]["content"]
    except (KeyError, IndexError, TypeError) as error:
        raise ReviewError("OpenRouter response did not contain review text") from error

    if isinstance(content, list):
        content = "".join(
            part.get("text", "") for part in content if isinstance(part, dict)
        )
    if not isinstance(content, str) or not content.strip():
        raise ReviewError("OpenRouter returned an empty review")
    return content


def call_openrouter(
    api_key: str,
    system_prompt: str,
    user_prompt: str,
    repository: str,
) -> str:
    response = request_json(
        "https://openrouter.ai/api/v1/chat/completions",
        method="POST",
        headers={
            "Authorization": f"Bearer {api_key}",
            "HTTP-Referer": f"https://github.com/{repository}",
            "X-OpenRouter-Title": "WhyLog CI AI Review",
        },
        payload={
            "model": OPENROUTER_MODEL,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": 0.1,
            "max_tokens": MAX_OUTPUT_TOKENS,
        },
    )
    return _extract_openrouter_text(response)


def _strip_code_fence(text: str) -> str:
    stripped = text.strip()
    match = re.fullmatch(r"```(?:json)?\s*(.*?)\s*```", stripped, re.DOTALL)
    return match.group(1) if match else stripped


def _validate_finding(value: Any, kind: str, index: int) -> Finding:
    if not isinstance(value, dict):
        raise ReviewError(f"{kind}[{index}] must be an object")

    required_strings = (
        "title",
        "file",
        "reason",
        "rule_reference",
        "recommendation",
    )
    parsed: dict[str, str] = {}
    for key in required_strings:
        field = value.get(key)
        if not isinstance(field, str) or not field.strip():
            raise ReviewError(f"{kind}[{index}].{key} must be a non-empty string")
        parsed[key] = field.strip()

    line = value.get("line")
    if line is not None and (
        not isinstance(line, int) or isinstance(line, bool) or line < 1
    ):
        raise ReviewError(f"{kind}[{index}].line must be null or a positive integer")

    return Finding(line=line, **parsed)


def parse_review(text: str) -> Review:
    try:
        payload = json.loads(_strip_code_fence(text))
    except json.JSONDecodeError as error:
        raise ReviewError("model output was not valid JSON") from error

    if not isinstance(payload, dict):
        raise ReviewError("model output must be a JSON object")
    summary = payload.get("summary")
    if not isinstance(summary, str) or not summary.strip():
        raise ReviewError("summary must be a non-empty string")

    findings: dict[str, tuple[Finding, ...]] = {}
    for kind in ("blocking", "suggestions"):
        values = payload.get(kind)
        if not isinstance(values, list):
            raise ReviewError(f"{kind} must be an array")
        if len(values) > MAX_FINDINGS_PER_KIND:
            raise ReviewError(f"{kind} exceeded the finding limit")
        findings[kind] = tuple(
            _validate_finding(value, kind, index) for index, value in enumerate(values)
        )

    return Review(
        summary=summary.strip(),
        blocking=findings["blocking"],
        suggestions=findings["suggestions"],
    )


def _provider_failure(name: str, error: Exception) -> str:
    return f"{name}: {type(error).__name__}: {error}"


def review_with_fallback(
    system_prompt: str,
    user_prompt: str,
    repository: str,
    gemini_api_key: str,
    openrouter_api_key: str,
) -> ProviderResult:
    failures: list[str] = []

    if gemini_api_key:
        try:
            raw = with_retry(
                lambda: call_gemini(gemini_api_key, system_prompt, user_prompt)
            )
            return ProviderResult("Google", GEMINI_MODEL, parse_review(raw))
        except (ReviewError, HttpRequestError, NetworkRequestError) as error:
            failures.append(_provider_failure("Gemini", error))
    else:
        failures.append("Gemini: GEMINI_API_KEY is not configured")

    if openrouter_api_key:
        try:
            raw = with_retry(
                lambda: call_openrouter(
                    openrouter_api_key,
                    system_prompt,
                    user_prompt,
                    repository,
                )
            )
            return ProviderResult(
                "OpenRouter",
                OPENROUTER_MODEL,
                parse_review(raw),
                fallback_reason=failures[-1],
            )
        except (ReviewError, HttpRequestError, NetworkRequestError) as error:
            failures.append(_provider_failure("OpenRouter", error))
    else:
        failures.append("OpenRouter: OPENROUTER_API_KEY is not configured")

    raise ReviewError("; ".join(failures))


def _safe_read(path: Path, workspace: Path) -> str:
    resolved = path.resolve()
    if workspace.resolve() not in resolved.parents:
        raise ReviewError(f"context path escaped the workspace: {path}")
    return path.read_text(encoding="utf-8", errors="replace")


def collect_context(workspace: Path) -> str:
    candidates = [
        workspace / "AGENTS.md",
        workspace / "ai" / "AGENTS.md",
        workspace / "server" / "AGENTS.md",
        workspace / "web" / "AGENTS.md",
    ]
    for docs_root in (
        workspace / "docs",
        workspace / "ai" / "docs",
        workspace / "server" / "docs",
        workspace / "web" / "docs",
    ):
        if docs_root.is_dir():
            candidates.extend(docs_root.rglob("*.md"))

    sections: list[str] = []
    used = 0
    for path in sorted(set(candidates)):
        if not path.is_file():
            continue
        relative_path = path.relative_to(workspace)
        if relative_path.parts[:2] == ("docs", "pr-reviews"):
            continue
        relative = relative_path.as_posix()
        content = _safe_read(path, workspace)
        if len(content) > MAX_CONTEXT_FILE_CHARS:
            content = content[:MAX_CONTEXT_FILE_CHARS] + "\n[파일 내용 잘림]"
        section = f"\n--- {relative} ---\n{content}"
        if used + len(section) > MAX_CONTEXT_CHARS:
            remaining = MAX_CONTEXT_CHARS - used
            if remaining > 100:
                sections.append(section[:remaining] + "\n[전체 컨텍스트 한도 도달]")
            break
        sections.append(section)
        used += len(section)

    if not sections:
        raise ReviewError("no trusted review context was found")
    return "".join(sections)


def github_request(
    api_url: str,
    token: str,
    path: str,
    *,
    method: str = "GET",
    payload: Any | None = None,
) -> Any:
    return with_retry(
        lambda: request_json(
            f"{api_url.rstrip('/')}{path}",
            method=method,
            headers={
                "Authorization": f"Bearer {token}",
                "Accept": "application/vnd.github+json",
                "X-GitHub-Api-Version": "2022-11-28",
            },
            payload=payload,
        )
    )


def fetch_pr_files(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    *,
    max_pages: int = 10,
) -> tuple[list[dict[str, Any]], bool]:
    files: list[dict[str, Any]] = []
    omitted = False
    for page in range(1, max_pages + 1):
        batch = github_request(
            api_url,
            token,
            f"/repos/{repository}/pulls/{pr_number}/files?per_page=100&page={page}",
        )
        if not isinstance(batch, list):
            raise ReviewError("GitHub returned an invalid pull request file list")
        files.extend(item for item in batch if isinstance(item, dict))
        if len(batch) < 100:
            break
        if page == max_pages:
            omitted = True
    return files, omitted


def build_diff_payload(
    files: list[dict[str, Any]],
    omitted_files: bool = False,
    *,
    max_patch_chars: int = MAX_PATCH_CHARS,
    max_diff_chars: int = MAX_DIFF_CHARS,
) -> str:
    records: list[dict[str, Any]] = []
    used = 0
    for item in files:
        filename = str(item.get("filename", "[unknown]"))
        patch = item.get("patch")
        if not isinstance(patch, str):
            patch = "[패치 없음: 바이너리 파일이거나 GitHub API 한도 초과]"
        elif len(patch) > max_patch_chars:
            patch = patch[:max_patch_chars] + "\n[파일 패치 잘림]"

        record = {
            "filename": filename,
            "previous_filename": item.get("previous_filename"),
            "status": item.get("status"),
            "additions": item.get("additions"),
            "deletions": item.get("deletions"),
            "patch": patch,
        }
        encoded = json.dumps(record, ensure_ascii=False)
        if used + len(encoded) > max_diff_chars:
            records.append(
                {
                    "notice": "전체 diff 입력 한도에 도달하여 이후 파일이 생략됨",
                    "first_omitted_file": filename,
                }
            )
            break
        records.append(record)
        used += len(encoded)

    if omitted_files:
        records.append({"notice": "GitHub 파일 조회 상한 이후 파일이 생략됨"})
    return json.dumps(records, ensure_ascii=False, indent=2)


def filter_reviewable_files(files: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return [
        item
        for item in files
        if not review_publishing.is_pr_review_doc_path(str(item.get("filename", "")))
    ]


def build_review_input_digest(trusted_context: str, diff_payload: str) -> str:
    return hashlib.sha256(
        f"{trusted_context}\n{diff_payload}".encode("utf-8")
    ).hexdigest()


def load_system_prompt(workspace: Path) -> str:
    prompt_path = workspace / SYSTEM_PROMPT_PATH
    if not prompt_path.is_file():
        raise ReviewError(f"trusted system prompt was not found: {SYSTEM_PROMPT_PATH}")
    prompt = _safe_read(prompt_path, workspace).strip()
    if not prompt:
        raise ReviewError("trusted system prompt was empty")
    return prompt


def build_user_prompt(
    pull_request: dict[str, Any],
    trusted_context: str,
    diff_payload: str,
) -> str:
    metadata = {
        "title": str(pull_request.get("title", "")),
        "body": str(pull_request.get("body") or "")[:MAX_PR_BODY_CHARS],
        "base": pull_request.get("base", {}).get("ref"),
        "head": pull_request.get("head", {}).get("ref"),
        "author": pull_request.get("user", {}).get("login"),
    }
    return f"""<TRUSTED_BASE_CONTEXT>
{trusted_context}
</TRUSTED_BASE_CONTEXT>

<UNTRUSTED_PR_METADATA_JSON>
{json.dumps(metadata, ensure_ascii=False, indent=2)}
</UNTRUSTED_PR_METADATA_JSON>

<UNTRUSTED_PR_DIFF_JSON>
{diff_payload}
</UNTRUSTED_PR_DIFF_JSON>

위 변경을 신뢰된 규칙에 맞춰 검토하고 지정된 JSON만 반환하라."""


def _render_findings(findings: tuple[Finding, ...]) -> str:
    if not findings:
        return "없음"
    sections: list[str] = []
    for index, finding in enumerate(findings, start=1):
        location = finding.file
        if finding.line is not None:
            location += f":{finding.line}"
        sections.append(
            f"{index}. **{finding.title}** (`{location}`)\n"
            f"   - 이유: {finding.reason}\n"
            f"   - 근거: {finding.rule_reference}\n"
            f"   - 수정: {finding.recommendation}"
        )
    return "\n\n".join(sections)


def provider_result_from_review_state(state: dict[str, Any]) -> ProviderResult:
    provider = state.get("provider")
    model = state.get("model")
    summary = state.get("summary")
    values = state.get("findings")
    if (
        not isinstance(provider, str)
        or not provider.strip()
        or not isinstance(model, str)
        or not model.strip()
        or not isinstance(summary, str)
        or not summary.strip()
        or not isinstance(values, list)
    ):
        raise ReviewError("signed PR review state is incomplete")

    grouped: dict[str, list[Finding]] = {"blocking": [], "suggestions": []}
    for index, value in enumerate(values[: 2 * MAX_FINDINGS_PER_KIND]):
        if not isinstance(value, dict):
            raise ReviewError("signed PR review finding must be an object")
        kind = value.get("kind")
        if kind not in grouped:
            raise ReviewError("signed PR review finding kind is invalid")
        grouped[kind].append(_validate_finding(value, kind, index))
    if any(len(items) > MAX_FINDINGS_PER_KIND for items in grouped.values()):
        raise ReviewError("signed PR review state exceeded the finding limit")

    return ProviderResult(
        provider.strip(),
        model.strip(),
        Review(
            summary.strip(),
            tuple(grouped["blocking"]),
            tuple(grouped["suggestions"]),
        ),
    )


def render_comment(
    result: ProviderResult,
    inline_result: review_publishing.InlinePublishResult | None = None,
    document_status: str | None = None,
) -> str:
    fallback = ""
    if result.fallback_reason:
        fallback = (
            "\n> Gemini 호출에 실패해 무료 OpenRouter 폴백을 사용했습니다: "
            f"`{result.fallback_reason}`\n"
        )
    verdict = "❌ 차단 항목 있음" if result.review.blocking else "✅ 차단 항목 없음"
    publishing = ""
    if inline_result is not None:
        publishing = (
            "\n**인라인:** "
            f"신규 {inline_result.posted} · 갱신 {inline_result.updated} · "
            f"재검출 안 됨 {inline_result.resolved} · "
            f"요약 대체 {len(inline_result.fallback_findings)}"
        )
        if inline_result.post_failed_fallback:
            publishing += " (GitHub가 줄 코멘트를 거부해 요약으로 대체)"
    if document_status:
        publishing += f"\n**PR 리뷰 문서:** {document_status}"
    return f"""{COMMENT_MARKER}
## WhyLog AI 리뷰

**결과:** {verdict} · **모델:** {result.provider} `{result.model}`
{fallback}
{result.review.summary}
{publishing}

### 차단

{_render_findings(result.review.blocking)}

### 제안

{_render_findings(result.review.suggestions)}

<sub>이 코멘트는 새 실행 때 갱신됩니다. 차단 항목은 사람이 타당성을 확인한 뒤 수정하세요.</sub>
"""


def render_failure_comment(message: str) -> str:
    return f"""{COMMENT_MARKER}
## WhyLog AI 리뷰

**결과:** ❌ 리뷰 실행 실패

`{message}`

Gemini와 OpenRouter 설정 또는 일시 장애를 확인하세요. 리뷰가 생성되지 않으면 quality gate는 통과하지 않습니다.
"""


def upsert_pr_comment(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    body: str,
) -> None:
    existing_id: int | None = None
    for page in range(1, 11):
        comments = github_request(
            api_url,
            token,
            f"/repos/{repository}/issues/{pr_number}/comments?per_page=100&page={page}",
        )
        if not isinstance(comments, list):
            raise ReviewError("GitHub returned an invalid comment list")
        for comment in comments:
            if not isinstance(comment, dict):
                continue
            author = comment.get("user", {}).get("type")
            if author == "Bot" and COMMENT_MARKER in str(comment.get("body", "")):
                existing_id = comment.get("id")
                break
        if existing_id is not None or len(comments) < 100:
            break

    if existing_id is None:
        github_request(
            api_url,
            token,
            f"/repos/{repository}/issues/{pr_number}/comments",
            method="POST",
            payload={"body": body},
        )
    else:
        github_request(
            api_url,
            token,
            f"/repos/{repository}/issues/comments/{existing_id}",
            method="PATCH",
            payload={"body": body},
        )


def _load_event(path: Path) -> tuple[int, dict[str, Any]]:
    try:
        event = json.loads(path.read_text(encoding="utf-8"))
        pull_request = event["pull_request"]
        pr_number = int(event["number"])
    except (OSError, json.JSONDecodeError, KeyError, TypeError, ValueError) as error:
        raise ReviewError(
            "GITHUB_EVENT_PATH did not contain a pull request event"
        ) from error
    if not isinstance(pull_request, dict):
        raise ReviewError("pull_request event payload was invalid")
    return pr_number, pull_request


def _assert_internal_pull_request(pull_request: dict[str, Any]) -> None:
    head_repo = pull_request.get("head", {}).get("repo", {}).get("full_name")
    base_repo = pull_request.get("base", {}).get("repo", {}).get("full_name")
    if not head_repo or head_repo != base_repo:
        raise ReviewError(
            "fork pull requests are not reviewed because GitHub does not expose repository secrets"
        )


def _assert_public_repository(is_private: str) -> None:
    if is_private.lower() == "true":
        raise ReviewError(
            "free-tier AI review is disabled for private repositories; "
            "review the provider data policy before enabling it"
        )


def _redact(message: str, secrets: tuple[str, ...]) -> str:
    redacted = message
    for secret in secrets:
        if secret:
            redacted = redacted.replace(secret, "***")
    return redacted[:1_000]


def run() -> int:
    event_path = Path(os.environ["GITHUB_EVENT_PATH"])
    workspace = Path(os.environ["GITHUB_WORKSPACE"])
    repository = os.environ["GITHUB_REPOSITORY"]
    api_url = os.environ.get("GITHUB_API_URL", "https://api.github.com")
    github_token = os.environ["GITHUB_TOKEN"]
    gemini_api_key = os.environ.get("GEMINI_API_KEY", "")
    openrouter_api_key = os.environ.get("OPENROUTER_API_KEY", "")
    push_token = os.environ.get("AI_REVIEW_PUSH_TOKEN", "")

    pr_number, pull_request = _load_event(event_path)
    _assert_internal_pull_request(pull_request)
    _assert_public_repository(os.environ.get("REPOSITORY_IS_PRIVATE", ""))
    head = pull_request.get("head", {})
    if not isinstance(head, dict):
        raise ReviewError("pull request head payload is invalid")
    head_ref = str(head.get("ref", ""))
    head_sha = str(head.get("sha", ""))
    if not head_ref or not head_sha:
        raise ReviewError("pull request head ref and sha are required")

    generated_doc_parent = review_publishing.generated_doc_only_parent_sha(
        api_url,
        github_token,
        repository,
        pull_request,
        github_request,
    )
    generated_doc_only = generated_doc_parent is not None
    files, omitted = fetch_pr_files(
        api_url,
        github_token,
        repository,
        pr_number,
    )
    reviewable_files = filter_reviewable_files(files)
    previous_document, _ = review_publishing.fetch_existing_review_doc(
        api_url,
        github_token,
        repository,
        pr_number,
        head_ref,
        github_request,
    )
    trusted_context = collect_context(workspace)
    diff_payload = build_diff_payload(reviewable_files, omitted)
    review_input_digest = build_review_input_digest(trusted_context, diff_payload)

    if generated_doc_parent and previous_document:
        previous_state = review_publishing.verified_pr_review_state(
            previous_document, push_token
        )
        if (
            previous_state
            and previous_state.get("head_sha") == generated_doc_parent
            and previous_state.get("review_input_digest") == review_input_digest
        ):
            preserved_result = provider_result_from_review_state(previous_state)
            review_publishing.write_local_review_doc(
                workspace, pr_number, previous_document
            )
            document_path = review_publishing.pr_review_doc_path(pr_number)
            upsert_pr_comment(
                api_url,
                github_token,
                repository,
                pr_number,
                render_comment(
                    preserved_result,
                    document_status=(
                        f"`{document_path}` 서명·부모 SHA·입력 digest 확인 · "
                        "이전 판단 유지"
                    ),
                ),
            )
            return 1 if preserved_result.review.blocking else 0

    system_prompt = load_system_prompt(workspace)
    user_prompt = build_user_prompt(
        pull_request,
        trusted_context,
        diff_payload,
    )
    result = review_with_fallback(
        system_prompt,
        user_prompt,
        repository,
        gemini_api_key,
        openrouter_api_key,
    )
    document = review_publishing.render_pr_review_document(
        pr_number,
        pull_request,
        result,
        previous_document,
        head_sha=head_sha,
        review_input_digest=review_input_digest,
        state_signing_secret=push_token,
    )
    review_publishing.write_local_review_doc(workspace, pr_number, document)
    if not review_publishing.pull_request_head_matches(
        api_url,
        github_token,
        repository,
        pr_number,
        head_ref,
        head_sha,
        github_request,
    ):
        print(
            "PR head changed during AI review; skipped stale inline comments and document sync."
        )
        return 1 if result.review.blocking else 0

    inline_result = review_publishing.publish_inline_review_comments(
        api_url,
        github_token,
        repository,
        pr_number,
        head_sha,
        reviewable_files,
        result,
        github_request,
    )

    document_path = review_publishing.pr_review_doc_path(pr_number)
    if generated_doc_only:
        document_status = f"`{document_path}` 자동 생성 커밋의 재실행이라 저장소 재동기화를 생략했습니다."
    elif not push_token:
        document_status = (
            f"`{document_path}` artifact 생성 · `AI_REVIEW_PUSH_TOKEN` 미설정"
        )
    elif head_ref in review_publishing.PROTECTED_DOC_SYNC_HEADS:
        document_status = (
            f"`{document_path}` artifact 생성 · 보호 브랜치 자동 커밋 생략"
        )
    else:
        document_status = f"`{document_path}` artifact 생성 · PR 브랜치 동기화 중"

    upsert_pr_comment(
        api_url,
        github_token,
        repository,
        pr_number,
        render_comment(result, inline_result, document_status),
    )

    if not generated_doc_only:
        try:
            sync_result = review_publishing.sync_pr_review_document(
                api_url,
                push_token,
                repository,
                pr_number,
                head_ref,
                document,
                workspace,
                github_request,
                expected_head_sha=head_sha,
            )
            status_labels = {
                "artifact-only": "artifact만 생성했습니다.",
                "protected-head-skipped": "보호 브랜치라 artifact만 생성했습니다.",
                "stale-head-skipped": "리뷰 도중 HEAD가 바뀌어 artifact만 생성했습니다.",
                "unchanged": "기존 저장소 문서와 동일합니다.",
                "synced": "PR 브랜치에 동기화했습니다.",
            }
            document_status = (
                f"`{sync_result.path}` "
                f"{status_labels.get(sync_result.mode, sync_result.mode)}"
            )
            upsert_pr_comment(
                api_url,
                github_token,
                repository,
                pr_number,
                render_comment(result, inline_result, document_status),
            )
        except Exception as error:
            message = _redact(
                f"{type(error).__name__}: {error}",
                (github_token, gemini_api_key, openrouter_api_key, push_token),
            )
            document_status = (
                f"`{document_path}` artifact만 생성 · 저장소 동기화 실패: `{message}`"
            )
            upsert_pr_comment(
                api_url,
                github_token,
                repository,
                pr_number,
                render_comment(result, inline_result, document_status),
            )
            return 1
    return 1 if result.review.blocking else 0


def main() -> int:
    secrets = (
        os.environ.get("GITHUB_TOKEN", ""),
        os.environ.get("GEMINI_API_KEY", ""),
        os.environ.get("OPENROUTER_API_KEY", ""),
        os.environ.get("AI_REVIEW_PUSH_TOKEN", ""),
    )
    try:
        return run()
    except Exception as error:
        message = _redact(f"{type(error).__name__}: {error}", secrets)
        print(f"AI review failed: {message}", file=sys.stderr)

        try:
            event_path = os.environ.get("GITHUB_EVENT_PATH")
            token = os.environ.get("GITHUB_TOKEN")
            repository = os.environ.get("GITHUB_REPOSITORY")
            if event_path and token and repository:
                pr_number, _ = _load_event(Path(event_path))
                upsert_pr_comment(
                    os.environ.get("GITHUB_API_URL", "https://api.github.com"),
                    token,
                    repository,
                    pr_number,
                    render_failure_comment(message),
                )
        except Exception as comment_error:
            comment_message = _redact(
                f"{type(comment_error).__name__}: {comment_error}", secrets
            )
            print(
                f"Could not publish the failure comment: {comment_message}",
                file=sys.stderr,
            )
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
