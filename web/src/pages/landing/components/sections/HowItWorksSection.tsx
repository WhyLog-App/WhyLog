import type { ReactNode } from "react";
import { HOW_STEPS } from "../../constants";

const STEP_ICONS: Record<number, ReactNode> = {
  1: (
    <path
      d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3zM19 12a7 7 0 0 1-14 0M12 19v3"
      stroke="#1E5BE8"
      strokeWidth="1.6"
      strokeLinecap="round"
    />
  ),
  2: (
    <>
      <circle cx="12" cy="12" r="9" stroke="#1E5BE8" strokeWidth="1.6" />
      <path
        d="M8 12h8M12 8v8"
        stroke="#1E5BE8"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
    </>
  ),
  3: (
    <>
      <path
        d="M5 4h11l3 3v13H5z"
        stroke="#1E5BE8"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
      <path
        d="M9 9h6M9 13h6M9 17h4"
        stroke="#1E5BE8"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
    </>
  ),
  4: (
    <>
      <circle cx="6" cy="6" r="2.4" stroke="#1E5BE8" strokeWidth="1.6" />
      <circle cx="6" cy="18" r="2.4" stroke="#1E5BE8" strokeWidth="1.6" />
      <circle cx="18" cy="12" r="2.4" stroke="#1E5BE8" strokeWidth="1.6" />
      <path
        d="M6 8.4v7.2M8 6.5c4 0 8 1.5 8 5.5"
        stroke="#1E5BE8"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
    </>
  ),
};

const HowItWorksSection = () => (
  <section
    id="how"
    className="relative overflow-hidden grain py-20 md:py-32 mesh-bg-soft"
  >
    <div className="relative max-w-7xl mx-auto px-6">
      <div className="text-center mb-14 md:mb-20 reveal">
        <p className="text-[12px] tracking-[0.22em] font-bold text-primary-500 uppercase mb-4">
          HOW IT WORKS
        </p>
        <h2 className="text-[32px] sm:text-[40px] md:text-[52px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.1]">
          4단계로 의사결정의
          <br />
          맥락이 살아납니다
        </h2>
      </div>

      <div className="relative">
        <svg
          viewBox="0 0 1200 200"
          preserveAspectRatio="none"
          className="absolute inset-x-0 top-1/2 -translate-y-1/2 w-full h-50 hidden md:block pointer-events-none"
          aria-hidden="true"
        >
          <defs>
            <linearGradient id="whylog-tube" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="#7AB7FF" stopOpacity="0" />
              <stop offset="20%" stopColor="#7AB7FF" stopOpacity="0.6" />
              <stop offset="50%" stopColor="#A78BFA" stopOpacity="0.7" />
              <stop offset="80%" stopColor="#F0ABFC" stopOpacity="0.6" />
              <stop offset="100%" stopColor="#F0ABFC" stopOpacity="0" />
            </linearGradient>
            <linearGradient id="whylog-tubeGlow" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="#1E5BE8" />
              <stop offset="50%" stopColor="#A78BFA" />
              <stop offset="100%" stopColor="#F0ABFC" />
            </linearGradient>
          </defs>
          <path
            d="M0,100 C200,40 400,160 600,100 C800,40 1000,160 1200,100"
            stroke="url(#whylog-tube)"
            strokeWidth="32"
            fill="none"
            strokeLinecap="round"
            opacity="0.5"
          />
          <path
            d="M0,100 C200,40 400,160 600,100 C800,40 1000,160 1200,100"
            stroke="url(#whylog-tube)"
            strokeWidth="14"
            fill="none"
            strokeLinecap="round"
          />
          <path
            d="M0,100 C200,40 400,160 600,100 C800,40 1000,160 1200,100"
            stroke="url(#whylog-tubeGlow)"
            strokeWidth="4"
            fill="none"
            strokeLinecap="round"
            className="tube-progress"
          />
        </svg>

        <div className="reveal-stagger relative grid grid-cols-1 md:grid-cols-4 gap-6">
          {HOW_STEPS.map((step) => (
            <article
              key={step.id}
              className={`glass card-lift rounded-3xl p-7 text-left relative ${
                step.offset ? "md:mt-10" : ""
              }`}
            >
              <div className="flex items-center justify-between mb-6">
                <div className="w-11 h-11 rounded-full bg-linear-to-br from-primary-500 to-primary-300 text-white flex items-center justify-center shadow-[0_8px_22px_rgba(30,91,232,0.35)] num font-bold text-[15px]">
                  {step.n}
                </div>
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                  <title>step icon</title>
                  {STEP_ICONS[step.n]}
                </svg>
              </div>
              <h3 className="text-[18px] font-bold text-ink-900 leading-tight">
                {step.title}
              </h3>
              <p className="mt-3 text-[13.5px] leading-[1.65] text-ink-500">
                {step.body}
              </p>
            </article>
          ))}
        </div>
      </div>
    </div>
  </section>
);

export default HowItWorksSection;
