import type { ReactNode } from "react";
import { PROBLEM_CARDS } from "../../constants";

const PROBLEM_ICONS: Record<string, ReactNode> = {
  "01": (
    <path
      d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"
      stroke="#1E5BE8"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  ),
  "02": (
    <>
      <circle cx="11" cy="11" r="7" stroke="#1E5BE8" strokeWidth="1.6" />
      <path
        d="m20 20-3.5-3.5"
        stroke="#1E5BE8"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
    </>
  ),
  "03": (
    <path
      d="M9 6 3 12l6 6M15 6l6 6-6 6"
      stroke="#1E5BE8"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  ),
};

const ProblemSection = () => (
  <section className="relative overflow-hidden grain py-20 md:py-32 mesh-bg-soft">
    <div className="shard w-24 h-24 left-[6%] top-[18%] float-c" />
    <div className="shard w-16 h-16 right-[10%] top-[22%] float-b" />
    <div className="shard w-14 h-14 left-[18%] bottom-[12%] float-a" />
    <div className="shard w-20 h-20 right-[18%] bottom-[18%] float-c" />
    <div className="shard w-10 h-10 left-[44%] top-[10%] float-b" />

    <div className="relative max-w-7xl mx-auto px-6 text-center">
      <p className="text-[12px] tracking-[0.22em] font-bold text-primary-500 uppercase mb-4 reveal">
        PROBLEM
      </p>
      <h2 className="reveal text-[32px] sm:text-[40px] md:text-[52px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.1]">
        이런 일, 한 번쯤 겪어보셨죠?
      </h2>
      <p className="reveal mt-5 text-[18px] text-ink-500 max-w-2xl mx-auto">
        회의에서 정한 결정이 시간이 지나면 사라집니다.
      </p>

      <div className="reveal-stagger mt-16 grid grid-cols-1 md:grid-cols-3 gap-6">
        {PROBLEM_CARDS.map((c) => (
          <article
            key={c.id}
            className="glass card-lift rounded-3xl p-6 sm:p-8 text-left"
          >
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-2xl bg-linear-to-br from-primary-50 to-white border border-white/80 flex items-center justify-center">
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  aria-hidden="true"
                >
                  {PROBLEM_ICONS[c.n]}
                </svg>
              </div>
              <span className="num text-[12px] text-ink-300 font-semibold">
                {c.n}
              </span>
            </div>
            <h3 className="text-[22px] font-bold text-ink-900 leading-tight">
              {c.title}
            </h3>
            <p className="mt-3 text-[15px] leading-[1.65] text-ink-500">
              {c.body}
            </p>
          </article>
        ))}
      </div>
    </div>
  </section>
);

export default ProblemSection;
