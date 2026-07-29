import { useEffect } from "react";
import { HOW_PROGRESS_END_RATIO, HOW_PROGRESS_START_RATIO } from "../constants";

/**
 * 스크롤 기반 효과 통합:
 *   1) `--scroll-y` CSS 변수 갱신
 *   2) `[data-parallax]` 요소의 `--py` 갱신 (속도 가중치)
 *   3) `.tube-progress` SVG path 의 `stroke-dashoffset` 진행도
 *
 * - scroll 리스너는 `{ passive: true }` 로 등록되어 메인 스레드를 차단하지 않음
 * - resize 는 layout 영향이 있으므로 onScroll 을 재호출해 길이/위치 재계산
 * - 언마운트 시 `--scroll-y` 변수도 함께 제거하여 다른 페이지로의 누수 차단
 */
export function useParallaxScroll() {
  useEffect(() => {
    const root = document.querySelector(
      ".whylog-landing",
    ) as HTMLElement | null;
    const parallaxNodes = Array.from(
      document.querySelectorAll<HTMLElement>(".whylog-landing [data-parallax]"),
    );
    const tube = document.querySelector(
      ".whylog-landing .tube-progress",
    ) as SVGPathElement | null;
    const howSection = document.getElementById("how");

    let totalLen = 0;
    if (tube) {
      totalLen = tube.getTotalLength();
      tube.style.strokeDasharray = String(totalLen);
      tube.style.strokeDashoffset = String(totalLen);
    }

    const onScroll = () => {
      const y = window.scrollY;
      if (root) root.style.setProperty("--scroll-y", `${y}`);
      for (const el of parallaxNodes) {
        const speed = Number(el.dataset.parallax) || 0;
        el.style.setProperty("--py", `${y * speed}px`);
      }
      if (tube && howSection) {
        const r = howSection.getBoundingClientRect();
        const vh = window.innerHeight;
        const start = vh * HOW_PROGRESS_START_RATIO;
        const end = -r.height + vh * HOW_PROGRESS_END_RATIO;
        const k = Math.max(0, Math.min(1, (start - r.top) / (start - end)));
        tube.style.strokeDashoffset = String(totalLen * (1 - k));
      }
    };
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => {
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("resize", onScroll);
      if (root) root.style.removeProperty("--scroll-y");
    };
  }, []);
}
