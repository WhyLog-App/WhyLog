import { useState } from "react";
import DiffViewer from "react-diff-viewer-continued";
import IconFileDocument from "@/assets/icons/file/ic_file_document.svg?react";
import { Icon } from "@/components/common/Icon";
import type { GitCommitDetailFile, GitCommitDetailFileLine } from "@/types/git";
import { parseUnifiedDiff } from "../utils/parseUnifiedDiff";
import DIFF_STYLES from "./styles/diffViewerStyles";

interface CommitChangedFileProps {
  file: GitCommitDetailFile;
}

const CommitChangedFile = ({ file }: CommitChangedFileProps) => {
  const [expanded, setExpanded] = useState(false);

  const toggle = () => setExpanded((s) => !s);

  const renderDiffContent = () => {
    if (file.lines.length === 1 && file.lines[0].type === "context") {
      const content = file.lines[0].content ?? "";
      const looksLikeUnified =
        /^(?:diff --git\s|index\s|@@\s|---\s|\+\+\+\s)/m.test(content);

      if (looksLikeUnified) {
        const { oldStr, newStr } = parseUnifiedDiff(content);
        return (
          <DiffViewer
            oldValue={oldStr}
            newValue={newStr}
            splitView={false}
            showDiffOnly={false}
            hideLineNumbers={false}
            styles={DIFF_STYLES}
          />
        );
      }

      return (
        <pre className="p-4 font-mono typo-caption1 text-(--color-text-primary) whitespace-pre-wrap">
          {content}
        </pre>
      );
    }

    return (
      <>
        {(file.lines || []).map(
          (line: GitCommitDetailFileLine, index: number) => (
            <div
              // biome-ignore lint/suspicious/noArrayIndexKey: diff 라인은 정적 스냅샷으로 순서/식별자가 고정됨
              key={`${file.fileName}-${index}`}
              className={`flex items-stretch border-b border-[#d4edda] last:border-b-0 min-h-[37px] ${
                line.type === "added"
                  ? "bg-[#e8f5e9]"
                  : line.type === "removed"
                    ? "bg-(--color-red-50)"
                    : "bg-[#f8fdf9]"
              }`}
            >
              <div className="w-[48px] shrink-0 flex items-center justify-end pr-3">
                <span className="font-mono typo-caption1 text-(--color-text-secondary)">
                  {line.lineNumber ?? ""}
                </span>
              </div>
              <div className="flex-1 flex items-center px-3">
                <pre
                  className={`font-mono typo-caption1 whitespace-pre-wrap ${
                    line.type === "added"
                      ? "text-[#2e7d32]"
                      : line.type === "removed"
                        ? "text-(--color-red-700)"
                        : "text-(--color-text-primary)"
                  }`}
                >
                  {line.content}
                </pre>
              </div>
            </div>
          ),
        )}
      </>
    );
  };

  return (
    <section className="overflow-hidden rounded-[12px] border border-(--color-indigo-50) bg-(--color-bg-surface) flex flex-col">
      <button
        type="button"
        onClick={toggle}
        aria-expanded={expanded}
        className="flex w-full items-center justify-between gap-4 bg-(--color-light-100) border-b border-(--color-indigo-50) px-5 py-3 text-left hover:bg-(--color-black-10)/5"
      >
        <div className="flex min-w-0 items-center gap-[6px]">
          <Icon
            icon={IconFileDocument}
            size={16}
            className="shrink-0 text-(--color-text-secondary)"
          />
          <p className="truncate font-mono typo-legend text-(--color-text-primary)">
            {file.fileName}
          </p>
        </div>
        <div className="flex items-center gap-2">
          {file.addedLines > 0 && (
            <span className="rounded-[4px] bg-[#e8f5e9] px-2 typo-caption1 font-semibold text-[#2e7d32] h-[22px] inline-flex items-center">
              +{file.addedLines}
            </span>
          )}
          {file.removedLines > 0 && (
            <span className="rounded-[4px] bg-(--color-red-50) px-2 typo-caption1 font-semibold text-(--color-red-700) h-[22px] inline-flex items-center">
              -{file.removedLines}
            </span>
          )}
        </div>
      </button>

      {expanded && (
        <div className="p-4">
          <div className="rounded-[8px] border border-(--color-light-700) overflow-hidden bg-[#f8fdf9]">
            {renderDiffContent()}
          </div>
        </div>
      )}
    </section>
  );
};

export default CommitChangedFile;
