interface CommitHashBadgeProps {
  hash: string;
  className?: string;
}

const CommitHashBadge = ({ hash, className = "" }: CommitHashBadgeProps) => (
  <span
    title={hash}
    className={`inline-flex items-center justify-center rounded bg-(--color-purple-50) px-1.5 py-0.5 font-mono typo-caption1 text-(--color-purple-700) ${className}`}
  >
    {hash.slice(0, 6)}
  </span>
);

export default CommitHashBadge;
