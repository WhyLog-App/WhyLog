import { memo, type ReactNode, useEffect, useMemo, useState } from "react";

export type TypeSegment = { text: string; gradient?: boolean } | { br: true };

type Props = {
  segments: TypeSegment[];
  speed?: number;
  startDelay?: number;
};

/**
 * Typewriter — 글자 단위 setState 를 일으키므로 별도 컴포넌트로 격리되어
 * `React.memo` 로 감싸 부모(`HeroSection`)의 다른 상태 변화에 영향받지 않게 한다.
 *
 * 렌더 전략: 모든 글자를 DOM 에 항상 두고 `visibility: hidden` 로 숨김 →
 * 첫 프레임부터 줄바꿈/길이가 확정되어 타이핑 중 레이아웃 이동이 없다.
 */
const Typewriter = ({ segments, speed = 75, startDelay = 250 }: Props) => {
  const total = useMemo(
    () =>
      segments.reduce(
        (n, s) => n + ("br" in s ? 0 : Array.from(s.text).length),
        0,
      ),
    [segments],
  );
  const [count, setCount] = useState(0);

  useEffect(() => {
    if (count >= total) return;
    const id = window.setTimeout(
      () => setCount((c) => Math.min(total, c + 1)),
      count === 0 ? startDelay : speed,
    );
    return () => window.clearTimeout(id);
  }, [count, total, speed, startDelay]);

  const done = count >= total;
  let globalIdx = 0;

  return (
    <>
      {segments.map((seg, i) => {
        if ("br" in seg) {
          // biome-ignore lint/suspicious/noArrayIndexKey: segments는 정적이며 위치가 곧 ID
          return <br key={`br-${i}`} />;
        }
        const chars = Array.from(seg.text);
        const startIdx = globalIdx;
        globalIdx += chars.length;
        const inner: ReactNode[] = [];
        chars.forEach((ch, j) => {
          const localIdx = startIdx + j;
          if (!done && count === localIdx) {
            inner.push(
              <span
                key={`caret-${localIdx}`}
                className="type-caret"
                aria-hidden
              />,
            );
          }
          inner.push(
            <span
              key={`g-${localIdx}`}
              style={{
                visibility: localIdx < count ? "visible" : "hidden",
              }}
            >
              {ch}
            </span>,
          );
        });
        return seg.gradient ? (
          // biome-ignore lint/suspicious/noArrayIndexKey: segments는 정적이며 위치가 곧 ID
          <span key={`s-${i}`} className="why-grad">
            {inner}
          </span>
        ) : (
          // biome-ignore lint/suspicious/noArrayIndexKey: segments는 정적이며 위치가 곧 ID
          <span key={`s-${i}`}>{inner}</span>
        );
      })}
      {done && <span className="type-caret done" aria-hidden />}
    </>
  );
};

export default memo(Typewriter);
