import type { GitRepositoryStats } from "../../../types/git";

interface GitHeaderProps {
  repositoryName: string;
  stats: GitRepositoryStats;
}

const GitHeader = ({ repositoryName, stats }: GitHeaderProps) => {
  return (
    <div className="flex flex-col gap-3">
      <p className="typo-h4 text-(--color-text-primary)">{repositoryName}</p>
      <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
        <div className="flex items-center gap-1">
          <span className="text-base font-semibold text-(--color-text-primary)">
            {stats.commits}
          </span>
          <span className="typo-body6 text-(--color-text-secondary)">
            Commits
          </span>
        </div>
        <span className="typo-body5 text-(--color-text-tertiary)">|</span>
        <div className="flex items-center gap-1">
          <span
            className="inline-flex h-2 w-2 rounded-full"
            style={{ backgroundColor: "#05a660" }}
          />
          <span className="typo-body6 text-(--color-text-secondary)">
            연결됨
          </span>
          <span
            className="typo-body5 font-semibold"
            style={{ color: "#05a660" }}
          >
            {stats.connected}
          </span>
        </div>
        <div className="flex items-center gap-1">
          <span
            className="inline-flex h-2 w-2 rounded-full"
            style={{ backgroundColor: "#e5b800" }}
          />
          <span className="typo-body6 text-(--color-text-secondary)">
            미연결
          </span>
          <span
            className="typo-body5 font-semibold"
            style={{ color: "#e5b800" }}
          >
            {stats.disconnected}
          </span>
        </div>
      </div>
    </div>
  );
};

export default GitHeader;
