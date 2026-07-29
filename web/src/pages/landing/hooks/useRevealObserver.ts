import { useEffect } from "react";

/**
 * 뷰포트에 들어온 `.reveal` / `.reveal-stagger` 엘리먼트에 `.in` 클래스를 부여.
 * 단일 IntersectionObserver 로 페이지 전체를 커버한다.
 */
export function useRevealObserver() {
  useEffect(() => {
    const targets = document.querySelectorAll(
      ".whylog-landing .reveal, .whylog-landing .reveal-stagger",
    );
    const obs = new IntersectionObserver(
      (entries) => {
        for (const e of entries) {
          if (e.isIntersecting) {
            e.target.classList.add("in");
            obs.unobserve(e.target);
          }
        }
      },
      { threshold: 0.15, rootMargin: "0px 0px -50px 0px" },
    );
    for (const el of targets) obs.observe(el);
    return () => obs.disconnect();
  }, []);
}
