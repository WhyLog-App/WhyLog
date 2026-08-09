from __future__ import annotations

import base64
import json
import re
import tempfile
import unittest
from pathlib import Path

import review_publishing as rp


class Obj:
    def __init__(self, **kwargs):
        self.__dict__.update(kwargs)


def finding(title: str, file: str, line: int | None) -> Obj:
    return Obj(
        title=title,
        file=file,
        line=line,
        reason="규칙 위반",
        rule_reference="server/AGENTS.md",
        recommendation="수정하세요",
    )


def result(blocking=(), suggestions=()) -> Obj:
    return Obj(
        provider="Google",
        model="gemini-3.6-flash",
        review=Obj(summary="검토 완료", blocking=blocking, suggestions=suggestions),
    )


def files() -> list[dict]:
    return [
        {
            "filename": "server/src/App.java",
            "patch": "@@ -1,3 +1,4 @@\n package a;\n+class App {}\n-old\n unchanged\n@@ -10,0 +12,2 @@\n+next\n+last",
        }
    ]


class FakeGitHub:
    def __init__(self, responses=None, errors=None):
        self.responses = list(responses or [])
        self.errors = list(errors or [])
        self.calls = []

    def __call__(self, api_url, token, path, *, method="GET", payload=None):
        self.calls.append((path, method, payload))
        if self.errors:
            error = self.errors.pop(0)
            if error is not None:
                raise error
        if self.responses:
            return self.responses.pop(0)
        return {}


class HttpError(Exception):
    def __init__(self, status):
        self.status = status
        super().__init__(f"HTTP {status}")


def review_threads_response(*threads: tuple[int, bool]) -> dict:
    return {
        "data": {
            "repository": {
                "pullRequest": {
                    "reviewThreads": {
                        "nodes": [
                            {
                                "id": f"THREAD_{comment_id}",
                                "isResolved": resolved,
                                "viewerCanResolve": not resolved,
                                "comments": {
                                    "nodes": [{"id": f"COMMENT_{comment_id}"}]
                                },
                            }
                            for comment_id, resolved in threads
                        ],
                        "pageInfo": {
                            "hasNextPage": False,
                            "endCursor": None,
                        },
                    }
                }
            }
        }
    }


def review_thread_mutation_response(comment_id: int) -> dict:
    return {
        "data": {
            "resolveReviewThread": {
                "thread": {
                    "id": f"THREAD_{comment_id}",
                    "isResolved": True,
                }
            }
        }
    }


class PatchParsingTest(unittest.TestCase):
    def test_parses_right_side_patch_lines(self):
        parsed = rp.parse_right_side_lines(files())

        self.assertEqual(parsed["server/src/App.java"], {1, 2, 3, 12, 13})

    def test_invalid_line_becomes_fallback(self):
        plan = rp.build_inline_review_plan(
            files(), result(blocking=(finding("x", "server/src/App.java", 99),))
        )

        self.assertEqual(plan.comments, ())
        self.assertEqual(
            plan.fallback_findings[0]["fallback_reason"],
            "not_a_valid_right_side_diff_line",
        )

    def test_groups_valid_blocking_and_suggestion_comments(self):
        review = result(
            blocking=(finding("block", "server/src/App.java", 2),),
            suggestions=(finding("suggest", "server/src/App.java", 12),),
        )

        plan = rp.build_inline_review_plan(files(), review)

        self.assertEqual(len(plan.comments), 2)
        self.assertEqual({comment.line for comment in plan.comments}, {2, 12})
        self.assertTrue(
            all("whylog-ai-inline-review" in comment.body for comment in plan.comments)
        )

    def test_groups_multiple_findings_on_same_line_into_one_comment(self):
        review = result(
            blocking=(finding("block", "server/src/App.java", 2),),
            suggestions=(finding("suggest", "server/src/App.java", 2),),
        )

        plan = rp.build_inline_review_plan(files(), review)

        self.assertEqual(len(plan.comments), 1)
        self.assertIn("1. **차단:", plan.comments[0].body)
        self.assertIn("2. **제안:", plan.comments[0].body)

    def test_inline_fingerprint_is_stable_for_same_location_when_text_changes(self):
        first = rp.build_inline_review_plan(
            files(), result(blocking=(finding("old text", "server/src/App.java", 2),))
        )
        second = rp.build_inline_review_plan(
            files(), result(blocking=(finding("new text", "server/src/App.java", 2),))
        )

        self.assertEqual(first.comments[0].fingerprint, second.comments[0].fingerprint)


class InlinePublishTest(unittest.TestCase):
    def test_keeps_current_comment_and_resolves_stale_without_rewriting(self):
        current = finding("block", "server/src/App.java", 2)
        fingerprint = (
            rp.build_inline_review_plan(files(), result(blocking=(current,)))
            .comments[0]
            .fingerprint
        )
        github = FakeGitHub(
            responses=[
                [
                    {
                        "id": 10,
                        "node_id": "COMMENT_10",
                        "body": f"<!-- whylog-ai-inline-review: {fingerprint} --> old",
                        "user": {"type": "Bot"},
                        "commit_id": "abc",
                        "path": "server/src/App.java",
                        "line": 2,
                        "side": "RIGHT",
                    },
                    {
                        "id": 11,
                        "node_id": "COMMENT_11",
                        "body": "<!-- whylog-ai-inline-review: 0123456789abcdef01234567 --> old",
                        "user": {"type": "Bot"},
                    },
                ],
                review_threads_response((10, False), (11, False)),
                review_thread_mutation_response(11),
            ]
        )

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "abc",
            files(),
            result(blocking=(current,)),
            github,
        )

        self.assertEqual(published.posted, 0)
        self.assertEqual(published.updated, 0)
        self.assertEqual(published.resolved, 1)
        self.assertEqual(github.calls[1][0], "/graphql")
        self.assertIn("resolveReviewThread", github.calls[2][2]["query"])
        self.assertFalse(any(call[1] == "PATCH" for call in github.calls))

    def test_posts_new_comments_individually_without_review_wrapper(self):
        github = FakeGitHub(responses=[[], {"id": 1}])

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "abc",
            files(),
            result(blocking=(finding("b", "server/src/App.java", 2),)),
            github,
        )

        self.assertTrue(published.created_comments)
        self.assertEqual(published.posted, 1)
        self.assertEqual(
            github.calls[-1][0], "/repos/WhyLog-App/WhyLog/pulls/7/comments"
        )
        self.assertTrue(
            github.calls[-1][2]["body"].startswith("<!-- whylog-ai-inline-review:")
        )
        self.assertIn("규칙 위반", github.calls[-1][2]["body"])
        self.assertEqual(github.calls[-1][2]["side"], "RIGHT")
        self.assertEqual(github.calls[-1][2]["line"], 2)
        self.assertEqual(github.calls[-1][2]["commit_id"], "abc")

    def test_existing_active_comments_are_left_unchanged(self):
        current = finding("block", "server/src/App.java", 2)
        fingerprint = (
            rp.build_inline_review_plan(files(), result(blocking=(current,)))
            .comments[0]
            .fingerprint
        )
        github = FakeGitHub(
            responses=[
                [
                    {
                        "id": 10,
                        "node_id": "COMMENT_10",
                        "body": f"<!-- whylog-ai-inline-review: {fingerprint} --> old",
                        "user": {"type": "Bot"},
                        "commit_id": "abc",
                        "path": "server/src/App.java",
                        "line": 2,
                        "side": "RIGHT",
                    },
                    {
                        "id": 11,
                        "node_id": "COMMENT_11",
                        "body": f"<!-- whylog-ai-inline-review: {fingerprint} --> duplicate",
                        "user": {"type": "Bot"},
                        "commit_id": "abc",
                        "path": "server/src/App.java",
                        "line": 2,
                        "side": "RIGHT",
                    },
                ]
            ]
        )

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "abc",
            files(),
            result(blocking=(current,)),
            github,
        )

        self.assertEqual(published.posted, 0)
        self.assertEqual(published.updated, 0)
        self.assertEqual(published.resolved, 0)
        self.assertEqual(len(github.calls), 1)

    def test_existing_finding_is_never_reopened_or_rewritten(self):
        current = finding("block", "server/src/App.java", 2)
        fingerprint = (
            rp.build_inline_review_plan(files(), result(blocking=(current,)))
            .comments[0]
            .fingerprint
        )
        github = FakeGitHub(
            responses=[
                [
                    {
                        "id": 10,
                        "node_id": "COMMENT_10",
                        "body": f"<!-- whylog-ai-inline-review: {fingerprint} --> old",
                        "user": {"type": "Bot"},
                        "commit_id": "abc",
                        "path": "server/src/App.java",
                        "line": 2,
                        "side": "RIGHT",
                    }
                ]
            ]
        )

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "abc",
            files(),
            result(blocking=(current,)),
            github,
        )

        self.assertEqual(published.posted, 0)
        self.assertEqual(published.updated, 0)
        self.assertEqual(published.resolved, 0)
        self.assertEqual(len(github.calls), 1)

    def test_already_resolved_stale_comment_is_left_unchanged(self):
        github = FakeGitHub(
            responses=[
                [
                    {
                        "id": 10,
                        "node_id": "COMMENT_10",
                        "body": "<!-- whylog-ai-inline-review: 0123456789abcdef01234567 --> old",
                        "user": {"type": "Bot"},
                    }
                ],
                review_threads_response((10, True)),
            ]
        )

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "abc",
            files(),
            result(),
            github,
        )

        self.assertEqual(published.resolved, 0)
        self.assertEqual(
            [call[0] for call in github.calls],
            [
                "/repos/WhyLog-App/WhyLog/pulls/7/comments?per_page=100&page=1",
                "/graphql",
            ],
        )
        self.assertFalse(any(call[1] == "PATCH" for call in github.calls))

    def test_resolution_permission_failure_does_not_rewrite_comment(self):
        threads = review_threads_response((10, False))
        thread = threads["data"]["repository"]["pullRequest"]["reviewThreads"][
            "nodes"
        ][0]
        thread["viewerCanResolve"] = False
        github = FakeGitHub(
            responses=[
                [
                    {
                        "id": 10,
                        "node_id": "COMMENT_10",
                        "body": "<!-- whylog-ai-inline-review: 0123456789abcdef01234567 --> old",
                        "user": {"type": "Bot"},
                    }
                ],
                threads,
            ]
        )

        with self.assertRaises(PermissionError):
            rp.publish_inline_review_comments(
                "api",
                "token",
                "WhyLog-App/WhyLog",
                7,
                "abc",
                files(),
                result(),
                github,
            )

        self.assertEqual(
            [call[0] for call in github.calls],
            [
                "/repos/WhyLog-App/WhyLog/pulls/7/comments?per_page=100&page=1",
                "/graphql",
            ],
        )

    def test_old_commit_comment_is_not_reposted_while_finding_remains(self):
        current = finding("block", "server/src/App.java", 2)
        fingerprint = (
            rp.build_inline_review_plan(files(), result(blocking=(current,)))
            .comments[0]
            .fingerprint
        )
        github = FakeGitHub(
            responses=[
                [
                    {
                        "id": 10,
                        "node_id": "COMMENT_10",
                        "body": f"<!-- whylog-ai-inline-review: {fingerprint} --> old",
                        "user": {"type": "Bot"},
                        "commit_id": "old-commit",
                        "path": "server/src/App.java",
                        "line": 2,
                        "side": "RIGHT",
                    }
                ]
            ]
        )

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "new-commit",
            files(),
            result(blocking=(current,)),
            github,
        )

        self.assertEqual(published.posted, 0)
        self.assertEqual(published.updated, 0)
        self.assertEqual(published.resolved, 0)
        self.assertEqual(len(github.calls), 1)

    def test_one_rejected_comment_does_not_drop_other_inline_comments(self):
        github = FakeGitHub(
            responses=[[], {"id": 20}],
            errors=[None, HttpError(422), None],
        )

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "abc",
            files(),
            result(
                blocking=(finding("b", "server/src/App.java", 2),),
                suggestions=(finding("s", "server/src/App.java", 12),),
            ),
            github,
        )

        self.assertTrue(published.created_comments)
        self.assertEqual(published.posted, 1)
        self.assertEqual(len(published.fallback_findings), 1)
        self.assertEqual(published.fallback_findings[0]["line"], 2)
        self.assertEqual(
            [call[0] for call in github.calls[1:]],
            [
                "/repos/WhyLog-App/WhyLog/pulls/7/comments",
                "/repos/WhyLog-App/WhyLog/pulls/7/comments",
            ],
        )

    def test_post_422_returns_fallback_instead_of_raising(self):
        github = FakeGitHub(responses=[[]], errors=[None, HttpError(422)])

        published = rp.publish_inline_review_comments(
            "api",
            "token",
            "WhyLog-App/WhyLog",
            7,
            "abc",
            files(),
            result(blocking=(finding("b", "server/src/App.java", 2),)),
            github,
        )

        self.assertFalse(published.created_comments)
        self.assertEqual(published.posted, 0)
        self.assertEqual(
            published.fallback_findings[-1]["fallback_reason"],
            "github_inline_comment_422",
        )
        self.assertIn("HTTP 422", published.post_failed_fallback or "")


class DocumentRenderingTest(unittest.TestCase):
    def pr(self):
        return {
            "title": "테스트 PR",
            "html_url": "https://github.com/WhyLog-App/WhyLog/pull/7",
            "user": {"login": "dev"},
            "base": {"ref": "develop"},
            "head": {"ref": "feature"},
        }

    def test_first_render_contains_state_marker(self):
        markdown = rp.render_pr_review_document(
            7,
            self.pr(),
            result(blocking=(finding("b", "server/src/App.java", 2),)),
            None,
            head_sha="abc",
            review_input_digest="digest",
        )

        self.assertIn("# PR-7 AI 리뷰 기록", markdown)
        self.assertIn("whylog-ai-pr-review-state", markdown)
        self.assertIn("- 상태: **BLOCKED**", markdown)
        self.assertIn("|new|차단|", markdown)
        self.assertIn("규칙 위반", markdown)
        self.assertEqual(self._state(markdown)["status"], "BLOCKED")

    def test_pass_status_is_visible_and_hidden_when_no_blocker(self):
        markdown = rp.render_pr_review_document(
            7,
            self.pr(),
            result(suggestions=(finding("s", "server/src/App.java", 2),)),
            None,
            head_sha="abc",
            review_input_digest="digest",
        )

        self.assertIn("- 상태: **PASS**", markdown)
        self.assertEqual(self._state(markdown)["status"], "PASS")

    def test_update_marks_new_ongoing_and_resolved(self):
        old = rp.render_pr_review_document(
            7,
            self.pr(),
            result(blocking=(finding("old", "server/src/App.java", 2),)),
            None,
            head_sha="oldsha",
            review_input_digest="old-digest",
        )

        new = rp.render_pr_review_document(
            7,
            self.pr(),
            result(
                blocking=(finding("old", "server/src/App.java", 2),),
                suggestions=(finding("new", "server/src/App.java", 12),),
            ),
            old,
            head_sha="newsha",
            review_input_digest="new-digest",
        )

        self.assertIn("|ongoing|차단|", new)
        self.assertIn("|new|제안|", new)
        self.assertIn("oldsha", new)

        resolved = rp.render_pr_review_document(
            7,
            self.pr(),
            result(),
            new,
            head_sha="finalsha",
            review_input_digest="final-digest",
        )
        self.assertIn("|resolved|", resolved)
        self.assertIn("finalsha", resolved)
        self.assertEqual(
            self._state(resolved)["resolved"][0]["resolved_by_head_sha"], "finalsha"
        )

        later = rp.render_pr_review_document(
            7,
            self.pr(),
            result(),
            resolved,
            head_sha="latersha",
            review_input_digest="later-digest",
        )
        self.assertIn("finalsha", later)
        self.assertEqual(len(self._state(later)["resolved"]), 2)

    def test_same_location_and_kind_stays_ongoing_when_wording_changes(self):
        old = rp.render_pr_review_document(
            7,
            self.pr(),
            result(blocking=(finding("old wording", "server/src/App.java", 2),)),
            None,
            head_sha="oldsha",
            review_input_digest="old-digest",
        )
        new = rp.render_pr_review_document(
            7,
            self.pr(),
            result(blocking=(finding("new wording", "server/src/App.java", 2),)),
            old,
            head_sha="newsha",
            review_input_digest="new-digest",
        )

        self.assertIn("|ongoing|차단|", new)
        self.assertNotIn("|new|차단|", new)
        self.assertNotIn("|resolved|차단|", new)

    def test_signed_state_verifies_and_tampering_fails(self):
        markdown = rp.render_pr_review_document(
            7,
            self.pr(),
            result(),
            None,
            head_sha="abc",
            review_input_digest="digest",
            state_signing_secret="secret-token",
        )

        self.assertIsNotNone(rp.verified_pr_review_state(markdown, "secret-token"))
        tampered = markdown.replace('"status":"PASS"', '"status":"BLOCKED"', 1)
        self.assertIsNone(rp.verified_pr_review_state(tampered, "secret-token"))

    def test_malformed_previous_state_is_ignored(self):
        markdown = rp.render_pr_review_document(
            7,
            self.pr(),
            result(blocking=(finding("b", "server/src/App.java", 2),)),
            "<!-- whylog-ai-pr-review-state {not-json} -->",
            head_sha="abc",
            review_input_digest="digest",
        )

        self.assertIn("|new|차단|", markdown)

    def _state(self, markdown):
        match = re.search(
            r"<!--\s*whylog-ai-pr-review-state\s+(\{.*?\})\s*-->",
            markdown,
            flags=re.DOTALL,
        )
        self.assertIsNotNone(match)
        return json.loads(match.group(1))


class ContentsSyncTest(unittest.TestCase):
    def test_fetch_404_returns_none(self):
        github = FakeGitHub(errors=[HttpError(404)])

        self.assertEqual(
            rp.fetch_existing_review_doc("api", "token", "repo", 7, "feature", github),
            (None, None),
        )

    def test_sync_no_token_is_artifact_only_and_writes_local(self):
        with tempfile.TemporaryDirectory() as directory:
            sync = rp.sync_pr_review_document(
                "api", "", "repo", 7, "feature", "doc", Path(directory), FakeGitHub()
            )

            self.assertEqual(sync.mode, "artifact-only")
            self.assertTrue((Path(directory) / "docs/pr-reviews/PR-7.md").is_file())

    def test_sync_protected_branch_skips_put(self):
        with tempfile.TemporaryDirectory() as directory:
            github = FakeGitHub()
            sync = rp.sync_pr_review_document(
                "api", "token", "repo", 7, "main", "doc", Path(directory), github
            )

            self.assertEqual(sync.mode, "protected-head-skipped")
            self.assertEqual(github.calls, [])

    def test_sync_unchanged_is_noop(self):
        encoded = base64.b64encode("doc".encode()).decode()
        github = FakeGitHub(
            responses=[
                {
                    "head": {
                        "ref": "feature",
                        "sha": "headsha",
                        "repo": {"full_name": "repo"},
                    }
                },
                {"content": encoded, "sha": "old"},
            ]
        )
        with tempfile.TemporaryDirectory() as directory:
            sync = rp.sync_pr_review_document(
                "api",
                "token",
                "repo",
                7,
                "feature",
                "doc",
                Path(directory),
                github,
                expected_head_sha="headsha",
            )

        self.assertEqual(sync.mode, "unchanged")
        self.assertFalse(sync.changed)

    def test_sync_creates_commit_from_reviewed_head(self):
        encoded = base64.b64encode("old".encode()).decode()
        github = FakeGitHub(
            responses=[
                {
                    "head": {
                        "ref": "feature",
                        "sha": "headsha",
                        "repo": {"full_name": "repo"},
                    }
                },
                {"content": encoded, "sha": "oldsha"},
                {"tree": {"sha": "base-tree"}},
                {"sha": "blob-sha"},
                {"sha": "new-tree"},
                {"sha": "newsha"},
                {"object": {"sha": "newsha"}},
            ]
        )
        with tempfile.TemporaryDirectory() as directory:
            sync = rp.sync_pr_review_document(
                "api",
                "token",
                "repo",
                7,
                "feature",
                "new",
                Path(directory),
                github,
                expected_head_sha="headsha",
            )

        self.assertEqual(sync.mode, "synced")
        self.assertEqual(sync.commit_sha, "newsha")
        self.assertEqual(github.calls[-1][1], "PATCH")
        self.assertEqual(github.calls[-1][2], {"sha": "newsha", "force": False})
        commit_call = github.calls[-2]
        self.assertEqual(commit_call[0], "/repos/repo/git/commits")
        self.assertEqual(commit_call[2]["parents"], ["headsha"])
        self.assertEqual(
            commit_call[2]["message"],
            "docs(docs): PR-7 리뷰 판단 근거 기록",
        )
        self.assertNotIn("author", commit_call[2])
        self.assertNotIn("committer", commit_call[2])

    def test_sync_stale_head_is_artifact_only(self):
        github = FakeGitHub(
            responses=[
                {
                    "head": {
                        "ref": "feature",
                        "sha": "newer-head",
                        "repo": {"full_name": "repo"},
                    }
                }
            ]
        )
        with tempfile.TemporaryDirectory() as directory:
            sync = rp.sync_pr_review_document(
                "api",
                "token",
                "repo",
                7,
                "feature",
                "doc",
                Path(directory),
                github,
                expected_head_sha="reviewed-head",
            )

        self.assertEqual(sync.mode, "stale-head-skipped")
        self.assertEqual(len(github.calls), 1)


class DocOnlyDetectionTest(unittest.TestCase):
    def test_detects_generated_doc_only_head_commit(self):
        github = FakeGitHub(
            responses=[
                {
                    "commit": {
                        "message": "docs(docs): PR-7 리뷰 판단 근거 기록"
                    },
                    "files": [{"filename": "docs/pr-reviews/PR-7.md"}],
                    "parents": [{"sha": "parent-sha"}],
                }
            ]
        )

        self.assertEqual(
            rp.generated_doc_only_parent_sha(
                "api", "token", "repo", {"head": {"sha": "abc"}}, github
            ),
            "parent-sha",
        )

    def test_rejects_non_review_doc_or_wrong_message(self):
        github = FakeGitHub(
            responses=[
                {
                    "commit": {"message": "docs: update"},
                    "files": [{"filename": "docs/pr-reviews/PR-7.md"}],
                    "parents": [{"sha": "parent-sha"}],
                }
            ]
        )

        self.assertIsNone(
            rp.generated_doc_only_parent_sha(
                "api", "token", "repo", {"head": {"sha": "abc"}}, github
            )
        )

    def test_rejects_mismatched_pr_number_in_generated_message(self):
        github = FakeGitHub(
            responses=[
                {
                    "commit": {
                        "message": "docs(docs): PR-8 리뷰 판단 근거 기록"
                    },
                    "files": [{"filename": "docs/pr-reviews/PR-7.md"}],
                    "parents": [{"sha": "parent-sha"}],
                }
            ]
        )

        self.assertIsNone(
            rp.generated_doc_only_parent_sha(
                "api", "token", "repo", {"head": {"sha": "abc"}}, github
            )
        )

    def test_rejects_invalid_review_doc_number_without_raising(self):
        github = FakeGitHub(
            responses=[
                {
                    "commit": {
                        "message": "docs(docs): PR-invalid 리뷰 판단 근거 기록"
                    },
                    "files": [{"filename": "docs/pr-reviews/PR-invalid.md"}],
                    "parents": [{"sha": "parent-sha"}],
                }
            ]
        )

        self.assertIsNone(
            rp.generated_doc_only_parent_sha(
                "api", "token", "repo", {"head": {"sha": "abc"}}, github
            )
        )

    def test_recognizes_pr_review_document_path(self):
        self.assertTrue(rp.is_pr_review_doc_path("docs/pr-reviews/PR-1.md"))
        self.assertFalse(rp.is_pr_review_doc_path("docs/pr-reviews/README.md"))


if __name__ == "__main__":
    unittest.main()
