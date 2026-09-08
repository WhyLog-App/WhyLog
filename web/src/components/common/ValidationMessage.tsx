interface ValidationMessageProps {
  message: string | null;
  tone?: "error" | "neutral";
}

export const ValidationMessage = ({
  message,
  tone = "error",
}: ValidationMessageProps) => {
  if (!message) return null;

  return (
    <p
      className={`typo-body6 ${tone === "error" ? "text-red-500" : "text-(--color-text-secondary)"}`}
      role={tone === "error" ? "alert" : "status"}
    >
      {message}
    </p>
  );
};
