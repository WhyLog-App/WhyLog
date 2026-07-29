import type { GitCommitItem } from "../../../types/git";
import CommitTableRow from "./CommitTableRow";

interface CommitTableProps {
  commits: GitCommitItem[];
  onRowClick?: (commit: GitCommitItem) => void;
}

const CommitTable = ({ commits, onRowClick }: CommitTableProps) => {
  return (
    <table className="w-full min-w-[720px] table-fixed border-collapse">
      <colgroup>
        <col className="w-[9%]" />
        <col className="w-[28%]" />
        <col className="w-[20%]" />
        <col className="w-[14%]" />
        <col className="w-[19%]" />
        <col className="w-[10%]" />
      </colgroup>
      <thead>
        <tr className="h-11 border-b border-(--color-light-700)">
          <th className="px-2 text-left align-middle">
            <span className="typo-caption1 font-normal uppercase tracking-[0.02em] text-(--color-text-secondary)">
              COMMIT
            </span>
          </th>
          <th className="px-2 text-left align-middle">
            <span className="typo-caption1 font-normal uppercase tracking-[0.02em] text-(--color-text-secondary)">
              MESSAGE
            </span>
          </th>
          <th className="px-2 text-left align-middle">
            <span className="typo-caption1 font-normal uppercase tracking-[0.02em] text-(--color-text-secondary)">
              연결된 Decision
            </span>
          </th>
          <th className="px-2 text-left align-middle">
            <span className="typo-caption1 font-normal uppercase tracking-[0.02em] text-(--color-text-secondary)">
              AUTHOR
            </span>
          </th>
          <th className="px-2 text-left align-middle">
            <span className="typo-caption1 font-normal uppercase tracking-[0.02em] text-(--color-text-secondary)">
              DATE
            </span>
          </th>
          <th className="px-2 text-right align-middle">
            <span className="typo-caption1 font-normal uppercase tracking-[0.02em] text-(--color-text-secondary)">
              CHANGES
            </span>
          </th>
        </tr>
      </thead>
      <tbody>
        {commits.map((commit) => (
          <CommitTableRow
            key={commit.id}
            commit={commit}
            onRowClick={onRowClick}
          />
        ))}
      </tbody>
    </table>
  );
};

export default CommitTable;
