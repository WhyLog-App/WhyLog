#!/usr/bin/env python3
"""Publish AI review results as inline PR comments and durable PR review docs.

This module intentionally accepts duck-typed review result objects instead of
importing ``ai_review.Finding`` / ``ProviderResult``. The CI entrypoint can pass
the existing dataclasses in, while this module remains free of circular imports.
"""

from __future__ import annotations

import base64
import hashlib
import hmac
import json
import re
import urllib.parse
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Mapping, Protocol, Sequence

INLINE_MARKER = "whylog-ai-inline-review"
DOC_STATE_MARKER = "whylog-ai-pr-review-state"
DOC_SIGNATURE_MARKER = "whylog-ai-pr-review-signature"
DOC_ROOT = "docs/pr-reviews"
MAX_BODY_CHARS = 2_000
MAX_MARKDOWN_FIELD_CHARS = 4_000
MAX_DOC_CHARS = 200_000
MAX_STATE_CHARS = 80_000
MAX_HISTORY = 20
PROTECTED_DOC_SYNC_HEADS = {"main", "develop"}

REVIEW_THREADS_QUERY = """
query WhyLogReviewThreads(
  $owner: String!
  $name: String!
  $number: Int!
  $after: String
) {
  repository(owner: $owner, name: $name) {
    pullRequest(number: $number) {
      reviewThreads(first: 100, after: $after) {
        nodes {
          id
          isResolved
          viewerCanResolve
          viewerCanUnresolve
          comments(first: 1) {
            nodes { id }
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
}
"""

RESOLVE_REVIEW_THREAD_MUTATION = """
mutation WhyLogResolveReviewThread($threadId: ID!) {
  resolveReviewThread(input: {threadId: $threadId}) {
    thread { id isResolved }
  }
}
"""

UNRESOLVE_REVIEW_THREAD_MUTATION = """
mutation WhyLogUnresolveReviewThread($threadId: ID!) {
  unresolveReviewThread(input: {threadId: $threadId}) {
    thread { id isResolved }
  }
}
"""


class GithubRequest(Protocol):
    def __call__(
        self,
        api_url: str,
        token: str,
        path: str,
        *,
        method: str = "GET",
        payload: Any | None = None,
    ) -> Any: ...


@dataclass(frozen=True)
class InlineComment:
    fingerprint: str
    kind: str
    path: str
    line: int
    body: str


@dataclass(frozen=True)
class InlineReviewPlan:
    comments: tuple[InlineComment, ...]
    fallback_findings: tuple[dict[str, Any], ...]


@dataclass(frozen=True)
class InlinePublishResult:
    created_comments: bool
    posted: int
    updated: int
    resolved: int
    fallback_findings: tuple[dict[str, Any], ...]
    post_failed_fallback: str | None = None


@dataclass(frozen=True)
class ReviewThreadState:
    id: str
    is_resolved: bool
    viewer_can_resolve: bool
    viewer_can_unresolve: bool


@dataclass(frozen=True)
class DocumentSyncResult:
    mode: str
    path: str
    changed: bool
    sha: str | None = None
    commit_sha: str | None = None


def _string(value: Any, default: str = "") -> str:
    return value if isinstance(value, str) else default


def _int(value: Any) -> int | None:
    return value if isinstance(value, int) and not isinstance(value, bool) else None


def _trim(value: Any, limit: int = MAX_MARKDOWN_FIELD_CHARS) -> str:
    text = str(value or "").replace("\r\n", "\n").replace("\r", "\n")
    text = re.sub(r"<!--.*?-->", "", text, flags=re.DOTALL)
    text = text.strip()
    if len(text) <= limit:
        return text
    return text[: limit - 20].rstrip() + "\n[내용 잘림]"


def _one_line(value: Any, limit: int = 180) -> str:
    return re.sub(r"\s+", " ", _trim(value, limit)).strip()


def _attr(obj: Any, name: str, default: Any = None) -> Any:
    if isinstance(obj, Mapping):
        return obj.get(name, default)
    return getattr(obj, name, default)


def _review(result: Any) -> Any:
    return _attr(result, "review", {})


def _findings(result: Any, kind: str) -> tuple[Any, ...]:
    values = _attr(_review(result), kind, ())
    if not isinstance(values, Sequence) or isinstance(values, (str, bytes, bytearray)):
        return ()
    return tuple(values)


def _finding_record(kind: str, finding: Any) -> dict[str, Any]:
    return {
        "kind": kind,
        "title": _one_line(_attr(finding, "title")),
        "file": _one_line(_attr(finding, "file")),
        "line": _int(_attr(finding, "line")),
        "reason": _trim(_attr(finding, "reason")),
        "rule_reference": _one_line(_attr(finding, "rule_reference"), 240),
        "recommendation": _trim(_attr(finding, "recommendation")),
    }


def _all_finding_records(result: Any) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    for kind in ("blocking", "suggestions"):
        records.extend(
            _finding_record(kind, finding) for finding in _findings(result, kind)
        )
    return records


def pr_review_doc_path(pr_number: int) -> str:
    if pr_number < 1:
        raise ValueError("pr_number must be positive")
    return f"{DOC_ROOT}/PR-{pr_number}.md"


def _pr_number_from_review_doc_path(path: str) -> int | None:
    match = re.fullmatch(r"docs/pr-reviews/PR-([1-9][0-9]*)\.md", path.strip())
    return int(match.group(1)) if match else None


def is_pr_review_doc_path(path: str) -> bool:
    return _pr_number_from_review_doc_path(path) is not None


def parse_right_side_lines(files: Sequence[Mapping[str, Any]]) -> dict[str, set[int]]:
    """Return right-side blob line numbers that are present in each PR patch."""
    parsed: dict[str, set[int]] = {}
    hunk = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@")
    for item in files:
        filename = _string(item.get("filename"))
        patch = item.get("patch")
        if not filename or not isinstance(patch, str):
            continue
        lines: set[int] = set()
        right_line: int | None = None
        for raw_line in patch.splitlines():
            match = hunk.match(raw_line)
            if match:
                right_line = int(match.group(1))
                continue
            if right_line is None or not raw_line:
                continue
            prefix = raw_line[0]
            if prefix == "+":
                if not raw_line.startswith("+++ "):
                    lines.add(right_line)
                right_line += 1
            elif prefix == " ":
                lines.add(right_line)
                right_line += 1
            elif prefix == "-":
                continue
            elif prefix == "\\":
                continue
        if lines:
            parsed[filename] = lines
    return parsed


def finding_fingerprint(kind: str, finding: Any) -> str:
    record = _finding_record(kind, finding)
    stable = {
        "kind": record["kind"],
        "file": record["file"],
        "line": record["line"],
    }
    digest = hashlib.sha256(
        json.dumps(stable, ensure_ascii=False, sort_keys=True).encode("utf-8")
    ).hexdigest()
    return digest[:24]


def _merge_document_finding(
    existing: Mapping[str, Any], incoming: Mapping[str, Any]
) -> dict[str, Any]:
    merged = dict(existing)
    for key in ("title", "reason", "rule_reference", "recommendation"):
        current = _trim(merged.get(key))
        additional = _trim(incoming.get(key))
        if not additional or additional == current:
            continue
        separator = " / " if key in {"title", "rule_reference"} else "\n\n"
        merged[key] = _trim(f"{current}{separator}{additional}")
    return merged


def _inline_marker(fingerprint: str) -> str:
    return f"<!-- {INLINE_MARKER}: {fingerprint} -->"


def _inline_body(records: Sequence[Mapping[str, Any]], fingerprint: str) -> str:
    sections = [f"{_inline_marker(fingerprint)}", "**WhyLog AI 리뷰**"]
    for index, record in enumerate(records, start=1):
        label = "차단" if record.get("kind") == "blocking" else "제안"
        sections.append(
            f"\n{index}. **{label}: {_one_line(record.get('title'))}**\n"
            f"   - 이유: {_trim(record.get('reason'), 600)}\n"
            f"   - 근거: `{_one_line(record.get('rule_reference'), 220)}`\n"
            f"   - 제안 수정: {_trim(record.get('recommendation'), 600)}"
        )
    return "\n".join(sections)[:MAX_BODY_CHARS]


def build_inline_review_plan(
    files: Sequence[Mapping[str, Any]],
    result: Any,
    *,
    max_comments: int = 50,
) -> InlineReviewPlan:
    valid_lines = parse_right_side_lines(files)
    grouped: dict[tuple[str, int], list[dict[str, Any]]] = {}
    fallback: list[dict[str, Any]] = []
    seen: set[str] = set()

    for kind in ("blocking", "suggestions"):
        for finding in _findings(result, kind):
            record = _finding_record(kind, finding)
            path = record["file"]
            line = record["line"]
            fingerprint = finding_fingerprint(kind, finding)
            record["fingerprint"] = fingerprint
            if (
                isinstance(line, int)
                and path in valid_lines
                and line in valid_lines[path]
                and fingerprint not in seen
            ):
                seen.add(fingerprint)
                grouped.setdefault((path, line), []).append(record)
            else:
                record["fallback_reason"] = "not_a_valid_right_side_diff_line"
                fallback.append(record)

    comments: list[InlineComment] = []
    for (path, line), records in grouped.items():
        if len(comments) >= max_comments:
            for record in records:
                overflow = dict(record)
                overflow["fallback_reason"] = "inline_comment_limit"
                fallback.append(overflow)
            continue
        grouped_fingerprint = inline_location_fingerprint(path, line)
        comments.append(
            InlineComment(
                fingerprint=grouped_fingerprint,
                kind="blocking"
                if any(record["kind"] == "blocking" for record in records)
                else "suggestions",
                path=path,
                line=line,
                body=_inline_body(records, grouped_fingerprint),
            )
        )

    return InlineReviewPlan(tuple(comments), tuple(fallback))


def _extract_fingerprint(body: Any) -> str | None:
    if not isinstance(body, str):
        return None
    match = re.search(
        rf"<!--\s*{re.escape(INLINE_MARKER)}:\s*([0-9a-f]{{24}})\s*-->", body
    )
    return match.group(1) if match else None


def _list_existing_inline_comments(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    github_request: GithubRequest,
) -> dict[str, list[Mapping[str, Any]]]:
    existing: dict[str, list[Mapping[str, Any]]] = {}
    for page in range(1, 11):
        comments = github_request(
            api_url,
            token,
            f"/repos/{repository}/pulls/{pr_number}/comments?per_page=100&page={page}",
        )
        if not isinstance(comments, list):
            return existing
        for comment in comments:
            if not isinstance(comment, Mapping):
                continue
            user = comment.get("user")
            user_type = user.get("type") if isinstance(user, Mapping) else None
            fingerprint = _extract_fingerprint(comment.get("body"))
            if user_type == "Bot" and fingerprint:
                existing.setdefault(fingerprint, []).append(comment)
        if len(comments) < 100:
            break
    return existing


def _is_status_error(error: Exception, status: int) -> bool:
    return getattr(error, "status", None) == status or f"HTTP {status}" in str(error)


def _github_graphql(
    api_url: str,
    token: str,
    query: str,
    variables: Mapping[str, Any],
    github_request: GithubRequest,
) -> Mapping[str, Any]:
    response = github_request(
        api_url,
        token,
        "/graphql",
        method="POST",
        payload={"query": query, "variables": dict(variables)},
    )
    if not isinstance(response, Mapping):
        raise ValueError("GitHub GraphQL returned an invalid response")
    errors = response.get("errors")
    if errors:
        raise ValueError(f"GitHub GraphQL returned errors: {_one_line(errors, 500)}")
    data = response.get("data")
    if not isinstance(data, Mapping):
        raise ValueError("GitHub GraphQL response did not contain data")
    return data


def _list_review_thread_states(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    github_request: GithubRequest,
) -> dict[str, ReviewThreadState]:
    owner, separator, name = repository.partition("/")
    if not separator or not owner or not name or "/" in name:
        raise ValueError("repository must use the owner/name format")

    states: dict[str, ReviewThreadState] = {}
    cursor: str | None = None
    for _ in range(10):
        data = _github_graphql(
            api_url,
            token,
            REVIEW_THREADS_QUERY,
            {
                "owner": owner,
                "name": name,
                "number": pr_number,
                "after": cursor,
            },
            github_request,
        )
        repository_data = data.get("repository")
        pull_request = (
            repository_data.get("pullRequest")
            if isinstance(repository_data, Mapping)
            else None
        )
        threads = (
            pull_request.get("reviewThreads")
            if isinstance(pull_request, Mapping)
            else None
        )
        if not isinstance(threads, Mapping):
            raise ValueError("GitHub GraphQL did not return pull request review threads")

        nodes = threads.get("nodes")
        if not isinstance(nodes, list):
            raise ValueError("GitHub GraphQL returned invalid review thread nodes")
        for node in nodes:
            if not isinstance(node, Mapping):
                continue
            thread_id = _string(node.get("id"))
            comments = node.get("comments")
            comment_nodes = (
                comments.get("nodes") if isinstance(comments, Mapping) else None
            )
            if not thread_id or not isinstance(comment_nodes, list):
                continue
            state = ReviewThreadState(
                id=thread_id,
                is_resolved=node.get("isResolved") is True,
                viewer_can_resolve=node.get("viewerCanResolve") is True,
                viewer_can_unresolve=node.get("viewerCanUnresolve") is True,
            )
            for comment in comment_nodes:
                if not isinstance(comment, Mapping):
                    continue
                comment_node_id = _string(comment.get("id"))
                if comment_node_id:
                    states[comment_node_id] = state

        page_info = threads.get("pageInfo")
        if not isinstance(page_info, Mapping):
            raise ValueError("GitHub GraphQL returned invalid review thread pagination")
        if page_info.get("hasNextPage") is not True:
            return states
        cursor = _string(page_info.get("endCursor")) or None
        if cursor is None:
            raise ValueError("GitHub GraphQL omitted the next review thread cursor")

    raise ValueError("GitHub GraphQL review thread pagination exceeded 10 pages")


def _set_review_thread_resolved(
    api_url: str,
    token: str,
    state: ReviewThreadState,
    resolved: bool,
    github_request: GithubRequest,
) -> None:
    if state.is_resolved == resolved:
        return
    if resolved:
        if not state.viewer_can_resolve:
            raise PermissionError("GitHub token cannot resolve this review thread")
        mutation = RESOLVE_REVIEW_THREAD_MUTATION
        operation = "resolveReviewThread"
    else:
        if not state.viewer_can_unresolve:
            raise PermissionError("GitHub token cannot unresolve this review thread")
        mutation = UNRESOLVE_REVIEW_THREAD_MUTATION
        operation = "unresolveReviewThread"

    data = _github_graphql(
        api_url,
        token,
        mutation,
        {"threadId": state.id},
        github_request,
    )
    result = data.get(operation)
    thread = result.get("thread") if isinstance(result, Mapping) else None
    if (
        not isinstance(thread, Mapping)
        or _string(thread.get("id")) != state.id
        or (thread.get("isResolved") is True) != resolved
    ):
        raise ValueError(f"GitHub GraphQL did not {operation} as requested")


def _set_inline_comment_thread_resolved(
    api_url: str,
    token: str,
    comment: Mapping[str, Any],
    thread_states: Mapping[str, ReviewThreadState],
    resolved: bool,
    github_request: GithubRequest,
) -> None:
    comment_node_id = _string(comment.get("node_id"))
    if not comment_node_id:
        raise ValueError("GitHub review comment did not contain a node_id")
    state = thread_states.get(comment_node_id)
    if state is None:
        raise ValueError("GitHub GraphQL did not return the review comment thread")
    _set_review_thread_resolved(
        api_url,
        token,
        state,
        resolved,
        github_request,
    )


def _inline_comment_matches_current_location(
    existing: Mapping[str, Any], planned: InlineComment, commit_id: str
) -> bool:
    return (
        _string(existing.get("commit_id")) == commit_id
        and _string(existing.get("path")) == planned.path
        and _int(existing.get("line")) == planned.line
        and _string(existing.get("side")) == "RIGHT"
    )


def publish_inline_review_comments(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    commit_id: str,
    files: Sequence[Mapping[str, Any]],
    result: Any,
    github_request: GithubRequest,
) -> InlinePublishResult:
    plan = build_inline_review_plan(files, result)
    existing = _list_existing_inline_comments(
        api_url, token, repository, pr_number, github_request
    )
    thread_states = (
        _list_review_thread_states(
            api_url, token, repository, pr_number, github_request
        )
        if existing
        else {}
    )
    current = {comment.fingerprint: comment for comment in plan.comments}
    updated = 0
    resolved = 0
    new_comments: list[InlineComment] = []

    for fingerprint, comment in current.items():
        matching_comments = [
            item
            for item in existing.get(fingerprint, [])
            if item.get("id") is not None
            and _inline_comment_matches_current_location(item, comment, commit_id)
        ]
        replaced_comments = [
            item
            for item in existing.get(fingerprint, [])
            if item.get("id") is not None and item not in matching_comments
        ]
        for replaced in replaced_comments:
            _set_inline_comment_thread_resolved(
                api_url,
                token,
                replaced,
                thread_states,
                True,
                github_request,
            )
            github_request(
                api_url,
                token,
                f"/repos/{repository}/pulls/comments/{replaced['id']}",
                method="PATCH",
                payload={
                    "body": (
                        "<!-- whylog-ai-inline-replaced -->\n"
                        "새 커밋의 같은 위치에 최신 자동 리뷰를 다시 등록했습니다."
                    )
                },
            )
            resolved += 1

        matching_comments.sort(key=lambda item: int(item["id"]))
        if matching_comments:
            old = matching_comments[-1]
            _set_inline_comment_thread_resolved(
                api_url,
                token,
                old,
                thread_states,
                False,
                github_request,
            )
            github_request(
                api_url,
                token,
                f"/repos/{repository}/pulls/comments/{old['id']}",
                method="PATCH",
                payload={"body": comment.body},
            )
            updated += 1
            for duplicate in matching_comments[:-1]:
                _set_inline_comment_thread_resolved(
                    api_url,
                    token,
                    duplicate,
                    thread_states,
                    True,
                    github_request,
                )
                github_request(
                    api_url,
                    token,
                    f"/repos/{repository}/pulls/comments/{duplicate['id']}",
                    method="PATCH",
                    payload={
                        "body": (
                            "<!-- whylog-ai-inline-duplicate -->\n"
                            "동일 위치의 중복 자동 리뷰를 최신 코멘트로 통합했습니다."
                        )
                    },
                )
                resolved += 1
        else:
            new_comments.append(comment)

    for fingerprint, old_comments in existing.items():
        if fingerprint in current:
            continue
        for old in old_comments:
            if old.get("id") is None:
                continue
            body = (
                "<!-- whylog-ai-inline-resolved -->\n"
                "현재 실행에서 재검출되지 않음(자동 추정). "
                "사람이 실제 반영 여부를 확인하세요."
            )
            _set_inline_comment_thread_resolved(
                api_url,
                token,
                old,
                thread_states,
                True,
                github_request,
            )
            github_request(
                api_url,
                token,
                f"/repos/{repository}/pulls/comments/{old['id']}",
                method="PATCH",
                payload={"body": body},
            )
            resolved += 1

    if not new_comments:
        return InlinePublishResult(False, 0, updated, resolved, plan.fallback_findings)

    posted = 0
    fallback = list(plan.fallback_findings)
    first_error: str | None = None
    for comment in new_comments:
        try:
            github_request(
                api_url,
                token,
                f"/repos/{repository}/pulls/{pr_number}/comments",
                method="POST",
                payload={
                    "commit_id": commit_id,
                    "path": comment.path,
                    "line": comment.line,
                    "side": "RIGHT",
                    "body": comment.body,
                },
            )
        except Exception as error:
            if _is_status_error(error, 422):
                fallback.append(
                    {
                        "kind": comment.kind,
                        "file": comment.path,
                        "line": comment.line,
                        "fingerprint": comment.fingerprint,
                        "fallback_reason": "github_inline_comment_422",
                    }
                )
                first_error = first_error or str(error)[:500]
                continue
            raise
        posted += 1

    return InlinePublishResult(
        posted > 0,
        posted,
        updated,
        resolved,
        tuple(fallback),
        first_error,
    )


def _encoded_state(state: Mapping[str, Any]) -> str:
    return json.dumps(state, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def _state_signature(encoded_state: str, signing_secret: str) -> str:
    derived_key = hmac.new(
        signing_secret.encode("utf-8"),
        b"whylog-ai-review-document-state-v1",
        hashlib.sha256,
    ).digest()
    return hmac.new(
        derived_key,
        encoded_state.encode("utf-8"),
        hashlib.sha256,
    ).hexdigest()


def _state_marker(state: Mapping[str, Any], signing_secret: str = "") -> str:
    encoded = _encoded_state(state)
    marker = f"<!-- {DOC_STATE_MARKER} {encoded} -->"
    if not signing_secret:
        return marker
    signature = _state_signature(encoded, signing_secret)
    return f"{marker}\n<!-- {DOC_SIGNATURE_MARKER} {signature} -->"


def inline_location_fingerprint(path: str, line: int) -> str:
    digest = hashlib.sha256(
        json.dumps(
            {"path": _one_line(path, 500), "line": line},
            ensure_ascii=False,
            sort_keys=True,
        ).encode("utf-8")
    ).hexdigest()
    return digest[:24]


def _parse_previous_state(markdown: str | None) -> dict[str, Any]:
    if not isinstance(markdown, str) or len(markdown) > MAX_DOC_CHARS:
        return {}
    match = re.search(
        rf"<!--\s*{re.escape(DOC_STATE_MARKER)}\s+(\{{.*?\}})\s*-->",
        markdown,
        flags=re.DOTALL,
    )
    if not match or len(match.group(1)) > MAX_STATE_CHARS:
        return {}
    try:
        state = json.loads(match.group(1))
    except json.JSONDecodeError:
        return {}
    return state if isinstance(state, dict) else {}


def verified_pr_review_state(
    markdown: str | None, signing_secret: str
) -> dict[str, Any] | None:
    if not signing_secret:
        return None
    state = _parse_previous_state(markdown)
    if not state or not isinstance(markdown, str):
        return None
    match = re.search(
        rf"<!--\s*{re.escape(DOC_SIGNATURE_MARKER)}\s+([0-9a-f]{{64}})\s*-->",
        markdown,
    )
    if not match:
        return None
    expected = _state_signature(_encoded_state(state), signing_secret)
    return state if hmac.compare_digest(match.group(1), expected) else None


def _state_findings_by_fingerprint(
    previous_state: Mapping[str, Any], key: str
) -> dict[str, dict[str, Any]]:
    values = previous_state.get(key)
    if not isinstance(values, list):
        return {}
    parsed: dict[str, dict[str, Any]] = {}
    for value in values[:200]:
        if not isinstance(value, dict):
            continue
        kind = value.get("kind")
        if kind not in {"blocking", "suggestions"}:
            continue
        record = _finding_record(kind, value)
        fingerprint = finding_fingerprint(kind, record)
        stored_fingerprint = value.get("fingerprint")
        if stored_fingerprint != fingerprint:
            continue
        record["fingerprint"] = fingerprint
        resolved_by = _one_line(value.get("resolved_by_head_sha"), 80)
        if resolved_by:
            record["resolved_by_head_sha"] = resolved_by
        parsed[fingerprint] = record
    return parsed


def _previous_findings_by_fingerprint(
    previous_state: Mapping[str, Any],
) -> dict[str, dict[str, Any]]:
    return _state_findings_by_fingerprint(previous_state, "findings")


def _previous_resolved_by_fingerprint(
    previous_state: Mapping[str, Any],
) -> dict[str, dict[str, Any]]:
    return _state_findings_by_fingerprint(previous_state, "resolved")


def _history(previous_state: Mapping[str, Any]) -> list[dict[str, Any]]:
    values = previous_state.get("history")
    if not isinstance(values, list):
        return []
    return [dict(item) for item in values[:MAX_HISTORY] if isinstance(item, dict)]


def _now() -> str:
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat()


def _pr_metadata(pull_request: Mapping[str, Any], pr_number: int) -> dict[str, Any]:
    head = (
        pull_request.get("head")
        if isinstance(pull_request.get("head"), Mapping)
        else {}
    )
    base = (
        pull_request.get("base")
        if isinstance(pull_request.get("base"), Mapping)
        else {}
    )
    user = (
        pull_request.get("user")
        if isinstance(pull_request.get("user"), Mapping)
        else {}
    )
    return {
        "number": pr_number,
        "title": _one_line(pull_request.get("title"), 240),
        "url": _one_line(pull_request.get("html_url"), 500),
        "author": _one_line(
            user.get("login") if isinstance(user, Mapping) else "", 120
        ),
        "base": _one_line(base.get("ref") if isinstance(base, Mapping) else "", 120),
        "head": _one_line(head.get("ref") if isinstance(head, Mapping) else "", 120),
    }


def _render_table(records: Sequence[Mapping[str, Any]], status: str) -> str:
    if not records:
        return "없음"
    rows = [
        "|상태|구분|위치|제목|이유|근거|권장 처리|반영 HEAD|",
        "|---|---|---|---|---|---|---|---|",
    ]
    for item in records:
        location = _one_line(item.get("file"), 160)
        if item.get("line"):
            location += f":{item['line']}"
        kind = "차단" if item.get("kind") == "blocking" else "제안"
        rows.append(
            "|"
            + "|".join(
                _escape_table(value)
                for value in (
                    status,
                    kind,
                    location,
                    item.get("title"),
                    item.get("reason"),
                    item.get("rule_reference"),
                    item.get("recommendation"),
                    item.get("resolved_by_head_sha", ""),
                )
            )
            + "|"
        )
    return "\n".join(rows)


def _escape_table(value: Any) -> str:
    text = _one_line(value, 280)
    return text.replace("|", "\\|")


def render_pr_review_document(
    pr_number: int,
    pull_request: Mapping[str, Any],
    result: Any,
    previous_markdown: str | None,
    *,
    head_sha: str,
    review_input_digest: str,
    state_signing_secret: str = "",
) -> str:
    previous_state = _parse_previous_state(previous_markdown)
    if state_signing_secret and not verified_pr_review_state(
        previous_markdown, state_signing_secret
    ):
        previous_state = {}
    previous_findings = _previous_findings_by_fingerprint(previous_state)
    previous_resolved = _previous_resolved_by_fingerprint(previous_state)
    current_by_fp: dict[str, dict[str, Any]] = {}
    for record in _all_finding_records(result):
        record = dict(record)
        record["fingerprint"] = finding_fingerprint(record["kind"], record)
        fingerprint = record["fingerprint"]
        if fingerprint in current_by_fp:
            current_by_fp[fingerprint] = _merge_document_finding(
                current_by_fp[fingerprint], record
            )
        else:
            current_by_fp[fingerprint] = record
    current = list(current_by_fp.values())

    new = [item for item in current if item["fingerprint"] not in previous_findings]
    ongoing = [item for item in current if item["fingerprint"] in previous_findings]
    newly_resolved = [
        {
            **item,
            "status": "resolved",
            "resolved_by_head_sha": head_sha,
        }
        for fingerprint, item in previous_findings.items()
        if fingerprint not in current_by_fp
    ]
    newly_resolved_fingerprints = {item["fingerprint"] for item in newly_resolved}
    still_resolved = [
        item
        for fingerprint, item in previous_resolved.items()
        if fingerprint not in current_by_fp
        and fingerprint not in newly_resolved_fingerprints
    ]
    resolved = [*newly_resolved, *still_resolved]

    provider = _one_line(_attr(result, "provider"), 120)
    model = _one_line(_attr(result, "model"), 160)
    summary = _trim(_attr(_review(result), "summary"), 3_000)
    status = (
        "BLOCKED" if any(item["kind"] == "blocking" for item in current) else "PASS"
    )
    metadata = _pr_metadata(pull_request, pr_number)
    generated_at = _now()
    history = [
        {
            "head_sha": head_sha,
            "generated_at": generated_at,
            "provider": provider,
            "model": model,
            "status": status,
            "total": len(current),
            "new": len(new),
            "ongoing": len(ongoing),
            "resolved": len(newly_resolved),
        },
        *_history(previous_state),
    ][:MAX_HISTORY]
    state = {
        "schema": 1,
        "pr": metadata,
        "head_sha": head_sha,
        "review_input_digest": _one_line(review_input_digest, 128),
        "provider": provider,
        "model": model,
        "summary": summary,
        "status": status,
        "findings": current,
        "resolved": resolved[:100],
        "history": history,
    }

    return (
        f"# PR-{pr_number} AI 리뷰 기록\n\n"
        f"- PR: {metadata['url'] or f'#{pr_number}'}\n"
        f"- 제목: {metadata['title']}\n"
        f"- 브랜치: `{metadata['base']}` ← `{metadata['head']}`\n"
        f"- HEAD: `{_one_line(head_sha, 80)}`\n"
        f"- 입력 digest: `{_one_line(review_input_digest, 128)}`\n"
        f"- 모델: {provider} `{model}`\n"
        f"- 상태: **{status}**\n"
        f"- 생성 시각(UTC): {generated_at}\n\n"
        "## 요약\n\n"
        f"{summary or '요약 없음'}\n\n"
        "## 이번 실행에서 새로 발견됨\n\n"
        f"{_render_table(new, 'new')}\n\n"
        "## 이전 실행부터 계속 남아있음\n\n"
        f"{_render_table(ongoing, 'ongoing')}\n\n"
        "## 현재까지 사라짐(자동 추정)\n\n"
        f"{_render_table(resolved, 'resolved')}\n\n"
        "## 실행 이력\n\n"
        f"{_render_history(history)}\n\n"
        f"{_state_marker(state, state_signing_secret)}\n"
    )


def _render_history(history: Sequence[Mapping[str, Any]]) -> str:
    if not history:
        return "없음"
    rows = [
        "|HEAD|상태|모델|전체|신규|계속|해결|시각|",
        "|---|---|---|---:|---:|---:|---:|---|",
    ]
    for item in history[:MAX_HISTORY]:
        rows.append(
            "|"
            + "|".join(
                _escape_table(value)
                for value in (
                    str(item.get("head_sha", ""))[:12],
                    item.get("status", ""),
                    f"{item.get('provider', '')} {item.get('model', '')}",
                    item.get("total", 0),
                    item.get("new", 0),
                    item.get("ongoing", 0),
                    item.get("resolved", 0),
                    item.get("generated_at", ""),
                )
            )
            + "|"
        )
    return "\n".join(rows)


def _contents_path(repository: str, path: str) -> str:
    return f"/repos/{repository}/contents/{path}"


def _decode_content_response(response: Any) -> tuple[str | None, str | None]:
    if not isinstance(response, Mapping):
        return None, None
    content = response.get("content")
    if not isinstance(content, str):
        return None, _string(response.get("sha")) or None
    try:
        text = base64.b64decode(content.encode("ascii"), validate=False).decode("utf-8")
    except Exception:
        text = None
    return text, _string(response.get("sha")) or None


def fetch_existing_review_doc(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    ref: str,
    github_request: GithubRequest,
) -> tuple[str | None, str | None]:
    try:
        response = github_request(
            api_url,
            token,
            f"{_contents_path(repository, pr_review_doc_path(pr_number))}?ref={urllib.parse.quote(ref, safe='')}",
        )
    except Exception as error:
        if _is_status_error(error, 404):
            return None, None
        raise
    return _decode_content_response(response)


def write_local_review_doc(workspace: Path, pr_number: int, markdown: str) -> Path:
    path = workspace / pr_review_doc_path(pr_number)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(markdown, encoding="utf-8")
    return path


def pull_request_head_matches(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    head_ref: str,
    expected_head_sha: str,
    github_request: GithubRequest,
) -> bool:
    pull_request = github_request(
        api_url,
        token,
        f"/repos/{repository}/pulls/{pr_number}",
    )
    head = (
        pull_request.get("head")
        if isinstance(pull_request, Mapping)
        and isinstance(pull_request.get("head"), Mapping)
        else {}
    )
    head_repo = head.get("repo") if isinstance(head.get("repo"), Mapping) else {}
    return (
        _string(head.get("ref")) == head_ref
        and _string(head.get("sha")) == expected_head_sha
        and _string(head_repo.get("full_name")) == repository
    )


def sync_pr_review_document(
    api_url: str,
    token: str,
    repository: str,
    pr_number: int,
    head_ref: str,
    markdown: str,
    workspace: Path,
    github_request: GithubRequest,
    *,
    expected_head_sha: str | None = None,
) -> DocumentSyncResult:
    path = pr_review_doc_path(pr_number)
    write_local_review_doc(workspace, pr_number, markdown)
    if not token:
        return DocumentSyncResult("artifact-only", path, True)
    if head_ref in PROTECTED_DOC_SYNC_HEADS:
        return DocumentSyncResult("protected-head-skipped", path, True)
    if not expected_head_sha:
        raise ValueError("expected_head_sha is required for repository sync")

    if not pull_request_head_matches(
        api_url,
        token,
        repository,
        pr_number,
        head_ref,
        expected_head_sha,
        github_request,
    ):
        return DocumentSyncResult("stale-head-skipped", path, True)

    existing, sha = fetch_existing_review_doc(
        api_url, token, repository, pr_number, head_ref, github_request
    )
    if existing == markdown:
        return DocumentSyncResult("unchanged", path, False, sha=sha)

    base_commit = github_request(
        api_url,
        token,
        f"/repos/{repository}/git/commits/{expected_head_sha}",
    )
    base_tree = base_commit.get("tree") if isinstance(base_commit, Mapping) else None
    base_tree_sha = (
        _string(base_tree.get("sha")) if isinstance(base_tree, Mapping) else ""
    )
    if not base_tree_sha:
        raise ValueError("GitHub did not return the reviewed commit tree")

    blob = github_request(
        api_url,
        token,
        f"/repos/{repository}/git/blobs",
        method="POST",
        payload={"content": markdown, "encoding": "utf-8"},
    )
    blob_sha = _string(blob.get("sha")) if isinstance(blob, Mapping) else ""
    if not blob_sha:
        raise ValueError("GitHub did not create the review document blob")

    tree = github_request(
        api_url,
        token,
        f"/repos/{repository}/git/trees",
        method="POST",
        payload={
            "base_tree": base_tree_sha,
            "tree": [
                {
                    "path": path,
                    "mode": "100644",
                    "type": "blob",
                    "sha": blob_sha,
                }
            ],
        },
    )
    tree_sha = _string(tree.get("sha")) if isinstance(tree, Mapping) else ""
    if not tree_sha:
        raise ValueError("GitHub did not create the review document tree")

    commit = github_request(
        api_url,
        token,
        f"/repos/{repository}/git/commits",
        method="POST",
        payload={
            "message": _doc_commit_message(pr_number),
            "tree": tree_sha,
            "parents": [expected_head_sha],
        },
    )
    commit_sha = _string(commit.get("sha")) if isinstance(commit, Mapping) else ""
    if not commit_sha:
        raise ValueError("GitHub did not create the review document commit")

    encoded_ref = urllib.parse.quote(head_ref, safe="/")
    github_request(
        api_url,
        token,
        f"/repos/{repository}/git/refs/heads/{encoded_ref}",
        method="PATCH",
        payload={"sha": commit_sha, "force": False},
    )
    return DocumentSyncResult("synced", path, True, sha=sha, commit_sha=commit_sha)


def _doc_commit_message(pr_number: int) -> str:
    return f"docs(docs): PR-{pr_number} 리뷰 판단 근거 기록"


def generated_doc_only_parent_sha(
    api_url: str,
    token: str,
    repository: str,
    pull_request: Mapping[str, Any],
    github_request: GithubRequest,
) -> str | None:
    head = (
        pull_request.get("head")
        if isinstance(pull_request.get("head"), Mapping)
        else {}
    )
    sha = _string(head.get("sha") if isinstance(head, Mapping) else "")
    if not sha:
        return None
    commit = github_request(api_url, token, f"/repos/{repository}/commits/{sha}")
    message = ""
    files = []
    if isinstance(commit, Mapping):
        inner = commit.get("commit")
        if isinstance(inner, Mapping):
            message = _string(inner.get("message"))
        raw_files = commit.get("files")
        if isinstance(raw_files, list):
            files = raw_files
    paths = [
        item.get("filename")
        for item in files
        if isinstance(item, Mapping) and isinstance(item.get("filename"), str)
    ]
    parents = commit.get("parents") if isinstance(commit, Mapping) else None
    if (
        len(paths) != 1
        or not is_pr_review_doc_path(paths[0])
        or not isinstance(parents, list)
        or len(parents) != 1
        or not isinstance(parents[0], Mapping)
    ):
        return None
    document_pr_number = _pr_number_from_review_doc_path(paths[0])
    if document_pr_number is None or message != _doc_commit_message(
        document_pr_number
    ):
        return None
    return _string(parents[0].get("sha")) or None


def is_generated_doc_only_commit(
    api_url: str,
    token: str,
    repository: str,
    pull_request: Mapping[str, Any],
    github_request: GithubRequest,
) -> bool:
    return (
        generated_doc_only_parent_sha(
            api_url, token, repository, pull_request, github_request
        )
        is not None
    )


def should_skip_doc_only_review_commit(
    api_url: str,
    token: str,
    repository: str,
    pull_request: Mapping[str, Any],
    github_request: GithubRequest,
) -> bool:
    """Backward-compatible alias for callers that treat generated doc commits specially."""
    return is_generated_doc_only_commit(
        api_url, token, repository, pull_request, github_request
    )
