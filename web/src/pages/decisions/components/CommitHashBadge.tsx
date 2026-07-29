interface CommitHashBadgeProps {
  hash: string;
  className?: string;
}

const CommitHashBadge = ({ hash, className = "" }: CommitHashBadgeProps) => (
  <span
    title={hash}
    className={`inline-flex items-center justify-center rounded bg-[#f5e5ff] px-3 py-0.5 font-mono text-[10px] leading-3.75 text-purple-700 ${className}`}
  >
    {hash.slice(0, 6)}
  </span>
);

export default CommitHashBadge;
