// 화자(member) 별로 일관된 색상을 부여하는 팔레트.
const MEMBER_COLOR_PALETTE = [
  "#2563eb", // blue-600
  "#dc2626", // red-600
  "#16a34a", // green-600
  "#d97706", // amber-600
  "#9333ea", // purple-600
  "#db2777", // pink-600
  "#0891b2", // cyan-600
  "#ea580c", // orange-600
  "#4f46e5", // indigo-600
  "#65a30d", // lime-600
] as const;

const FALLBACK_COLOR = "#6b7280"; // gray-500

/**
 * memberId 가 같으면 항상 같은 색을 반환.
 * memberId 가 null/undefined 이면 fromName 을 fallback 키로 사용
 */
export const getMemberColor = (
  memberId: number | string | null | undefined,
  fallbackKey?: string,
): string => {
  const key = memberId ?? fallbackKey;
  if (key == null || key === "") return FALLBACK_COLOR;

  const numeric =
    typeof key === "number"
      ? key
      : Array.from(String(key)).reduce(
          (acc, ch) => (acc * 31 + ch.charCodeAt(0)) >>> 0,
          0,
        );
  const idx = Math.abs(numeric) % MEMBER_COLOR_PALETTE.length;
  return MEMBER_COLOR_PALETTE[idx];
};
