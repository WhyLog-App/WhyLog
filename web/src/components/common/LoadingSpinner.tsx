import LoadingSpinnerUrl from "@/assets/illustrations/empty_state_illustration.svg";

interface LoadingSpinnerProps {
  className?: string;
  size?: number;
}

const LoadingSpinner = ({
  className = "",
  size = 160,
}: LoadingSpinnerProps) => {
  return (
    <div
      className={`flex h-full min-h-screen w-full items-center justify-center ${className}`}
      role="status"
      aria-live="polite"
    >
      <img
        src={LoadingSpinnerUrl}
        alt=""
        aria-hidden="true"
        width={size}
        height={size}
      />
    </div>
  );
};

export default LoadingSpinner;
