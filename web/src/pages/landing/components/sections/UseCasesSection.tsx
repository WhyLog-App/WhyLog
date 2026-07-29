import { USE_CASES } from "../../constants";

const UseCasesSection = () => (
  <section
    id="usecases"
    className="relative overflow-hidden grain py-20 md:py-32 mesh-bg-soft"
  >
    <div className="relative max-w-7xl mx-auto px-6">
      <div className="text-center mb-16 reveal">
        <p className="text-[12px] tracking-[0.22em] font-bold text-primary-500 uppercase mb-4">
          USE CASES
        </p>
        <h2 className="text-[32px] sm:text-[40px] md:text-[52px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.1]">
          이런 팀에 잘 어울려요
        </h2>
      </div>

      <div className="reveal-stagger grid grid-cols-1 md:grid-cols-3 gap-6">
        {USE_CASES.map((c) => (
          <article key={c.id} className="glass card-lift rounded-3xl p-6 sm:p-8">
            <div
              className="w-14 h-14 rounded-full mb-6 flex items-center justify-center text-white font-bold text-[16px]"
              style={{ background: c.gradient, boxShadow: c.shadow }}
            >
              {c.tag}
            </div>
            <p className="text-[12px] font-semibold tracking-wider text-primary-500 uppercase">
              {c.kicker}
            </p>
            <h3 className="mt-1 text-[22px] font-bold text-ink-900 leading-tight">
              {c.title}
            </h3>
            <p className="mt-3 text-[14.5px] leading-[1.7] text-ink-500">
              {c.body}
            </p>
            <div className="hairline my-6" />
            <div className="flex items-center gap-2 text-[12px] text-ink-300">
              {c.statPrefix}
              <span className="num font-bold text-ink-700">{c.statValue}</span>
              {c.statSuffix}
            </div>
          </article>
        ))}
      </div>
    </div>
  </section>
);

export default UseCasesSection;
