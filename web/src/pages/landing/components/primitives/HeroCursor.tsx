import { memo, useEffect, useRef } from "react";

/**
 * 랜딩페이지 전체에서 마우스를 따라 lerp 로 움직이는 iridescent blob.
 * position: fixed 기반이라 viewport 좌표(clientX/Y)를 그대로 사용한다.
 * 모든 좌표 갱신은 RAF + style.transform 으로 처리 — React state 미사용.
 */
const HeroCursor = () => {
  const dotRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    // 마우스가 없는 터치/coarse 포인터 기기에서는 커서 블롭을 띄우지 않는다.
    if (!window.matchMedia("(pointer: fine)").matches) return;

    const root = document.querySelector(
      ".whylog-landing",
    ) as HTMLElement | null;
    const dot = dotRef.current;
    if (!root || !dot) return;

    let half = 9;
    const state = { x: 0, y: 0, tx: 0, ty: 0, started: false };
    let raf = 0;

    const tick = () => {
      state.x += (state.tx - state.x) * 0.18;
      state.y += (state.ty - state.y) * 0.18;
      half = dot.offsetWidth / 2;
      dot.style.transform = `translate3d(${state.x - half}px, ${state.y - half}px, 0)`;
      raf = requestAnimationFrame(tick);
    };
    const startLoop = () => {
      if (raf) return;
      raf = requestAnimationFrame(tick);
    };
    const stopLoop = () => {
      if (!raf) return;
      cancelAnimationFrame(raf);
      raf = 0;
    };

    const onMove = (e: MouseEvent) => {
      state.tx = e.clientX;
      state.ty = e.clientY;
      if (!state.started) {
        state.x = state.tx;
        state.y = state.ty;
        state.started = true;
        dot.style.opacity = "1";
        startLoop();
      }
    };
    const onLeave = () => {
      dot.style.opacity = "0";
      state.started = false;
      stopLoop();
    };
    const onOver = (e: MouseEvent) => {
      const t = e.target as HTMLElement | null;
      if (t?.closest("button, a")) dot.classList.add("is-hover");
      else dot.classList.remove("is-hover");
    };

    root.addEventListener("mousemove", onMove, { passive: true });
    root.addEventListener("mouseleave", onLeave);
    root.addEventListener("mouseover", onOver);

    return () => {
      stopLoop();
      root.removeEventListener("mousemove", onMove);
      root.removeEventListener("mouseleave", onLeave);
      root.removeEventListener("mouseover", onOver);
    };
  }, []);

  return <div ref={dotRef} className="iri-morph hero-cursor" aria-hidden />;
};

export default memo(HeroCursor);
