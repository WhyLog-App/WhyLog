interface ConfidenceBadgeProps {
  score: number | null;
}

const ConfidenceBadge = ({ score }: ConfidenceBadgeProps) => {
  if (score == null) return null;

  return (
    <span className="inline-flex items-center justify-center rounded-full bg-blue-50 px-3 py-0.5 typo-caption2 font-bold text-blue-700">
      신뢰도 {score}점
    </span>
  );
};

export default ConfidenceBadge;
