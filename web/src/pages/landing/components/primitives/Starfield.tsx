import { type CSSProperties, memo, useMemo } from "react";

type Props = { count?: number };

/**
 * 결정적 PRNG 로 별 위치를 생성 → 같은 count 면 항상 같은 패턴.
 * `React.memo` + `useMemo` 로 부모 리렌더에서도 별 위치는 재계산되지 않는다.
 */
const Starfield = ({ count = 36 }: Props) => {
  const stars = useMemo(() => {
    const rand = (seed: number) => {
      const x = Math.sin(seed * 9301 + 49297) * 233280;
      return x - Math.floor(x);
    };
    return Array.from({ length: count }, (_, i) => ({
      top: `${(rand(i + 1) * 100).toFixed(2)}%`,
      left: `${(rand(i + 200) * 100).toFixed(2)}%`,
      size: 1 + Math.round(rand(i + 400) * 2),
      delay: (rand(i + 600) * 4).toFixed(2),
      duration: (3 + rand(i + 800) * 3).toFixed(2),
      opacity: 0.45 + rand(i + 1000) * 0.5,
    }));
  }, [count]);
  return (
    <div className="absolute inset-0 pointer-events-none overflow-hidden">
      {stars.map((s, i) => (
        <span
          // biome-ignore lint/suspicious/noArrayIndexKey: 결정적 PRNG로 인덱스가 곧 ID
          key={i}
          className="star"
          style={
            {
              top: s.top,
              left: s.left,
              width: `${s.size}px`,
              height: `${s.size}px`,
              animationDelay: `${s.delay}s`,
              animationDuration: `${s.duration}s`,
              "--star-op": s.opacity,
            } as CSSProperties
          }
        />
      ))}
    </div>
  );
};

export default memo(Starfield);
