import { memo, useEffect, useRef } from "react";

type Props = {
  to: number;
  duration?: number;
  suffix?: string;
};

/**
 * Count-up — 뷰포트에 들어오면 0 → `to` 로 ease-out 카운트.
 * Observer는 본인 엘리먼트 하나만 보고, 카운트 완료 후 즉시 disconnect 된다.
 * 텍스트는 DOM 에 직접 쓰기 때문에 부모 리렌더를 트리거하지 않는다.
 */
const CountUp = ({ to, duration = 1600, suffix = "" }: Props) => {
  const ref = useRef<HTMLSpanElement>(null);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    let rafId = 0;
    el.textContent = `0${suffix}`;
    const obs = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (!entry?.isIntersecting) return;
        obs.disconnect();
        const start = performance.now();
        const tick = (t: number) => {
          const k = Math.min(1, (t - start) / duration);
          const eased = 1 - (1 - k) ** 3;
          const v = Math.round(to * eased);
          el.textContent = `${v}${suffix}`;
          if (k < 1) rafId = requestAnimationFrame(tick);
        };
        rafId = requestAnimationFrame(tick);
      },
      { threshold: 0.4 },
    );
    obs.observe(el);
    return () => {
      obs.disconnect();
      if (rafId) cancelAnimationFrame(rafId);
    };
  }, [to, duration, suffix]);
  return <span ref={ref}>0{suffix}</span>;
};

export default memo(CountUp);
