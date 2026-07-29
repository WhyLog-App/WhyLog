import type {
  CommitDetailResult,
  GitCommitDetailFile,
  GitCommitDetailItem,
} from "@/types/git";
import { formatCommitDate } from "@/utils/date";

export const mapCommitDetail = (
  repositoryName: string,
  apiResult: CommitDetailResult | undefined,
  commitHash: string,
): GitCommitDetailItem => {
  if (!apiResult) {
    return {
      repositoryName,
      hash: commitHash,
      message: "",
      description: "",
      authorName: "",
      authorEmail: "",
      dateText: "",
      decisionText: "",
      decisionType: "neutral",
      changesAdded: 0,
      changesRemoved: 0,
      files: [],
    };
  }

  const files: GitCommitDetailFile[] = (apiResult.changed_file_list ?? []).map(
    (file) => ({
      fileName: file.file_name,
      addedLines: file.added_lines,
      removedLines: file.deleted_lines,
      lines: [
        {
          type: "context" as const,
          content: file.changed_code,
        },
      ],
    }),
  );

  return {
    repositoryName,
    hash: apiResult.hash ?? commitHash,
    message: apiResult.message ?? "",
    description: apiResult.description ?? "",
    authorName: apiResult.author_name ?? "",
    authorEmail: apiResult.author_email ?? "",
    authorProfileImage: apiResult.author_profile_image ?? undefined,
    dateText: formatCommitDate(apiResult.date_time ?? ""),
    decisionText: "",
    decisionType: "neutral",
    changesAdded: files.reduce((s, f) => s + f.addedLines, 0),
    changesRemoved: files.reduce((s, f) => s + f.removedLines, 0),
    files,
  };
};
