/**
 * URL path 파라미터로 들어온 문자열을 라우트 ID(양의 정수)로 변환한다.
 * 유효하지 않은 값(undefined, NaN, 음수, 0, 비정수, 안전하지 않은 정수)은 모두 null을 반환한다.
 */
export const parseRouteId = (raw: string | undefined): number | null => {
  if (!raw || !/^\d+$/.test(raw)) return null;
  const num = Number(raw);
  if (!Number.isSafeInteger(num) || num <= 0) return null;
  return num;
};
