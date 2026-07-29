import { FAQ_ITEMS } from "../../constants";

const FaqSection = () => (
  <section id="faq" className="relative overflow-hidden grain py-20 md:py-32">
    <div className="relative max-w-3xl mx-auto px-6">
      <div className="text-center mb-12 reveal">
        <p className="text-[12px] tracking-[0.22em] font-bold text-primary-500 uppercase mb-4">
          FAQ
        </p>
        <h2 className="text-[32px] sm:text-[40px] md:text-[48px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.1]">
          자주 묻는 질문
        </h2>
      </div>

      <div className="reveal-stagger space-y-3">
        {FAQ_ITEMS.map((f) => (
          <details
            key={f.id}
            className="glass rounded-2xl px-5 sm:px-6 py-5 group"
            {...(f.open ? { open: true } : {})}
          >
            <summary className="flex items-center justify-between gap-4">
              <span className="text-[16px] font-semibold text-ink-900">
                {f.q}
              </span>
              <span
                aria-hidden="true"
                className="faq-icon w-7 h-7 rounded-full bg-primary-50 text-primary-500 flex items-center justify-center text-[16px] font-bold"
              >
                +
              </span>
            </summary>
            <p className="mt-4 text-[14.5px] leading-[1.7] text-ink-500">
              {f.a}
            </p>
          </details>
        ))}
      </div>
    </div>
  </section>
);

export default FaqSection;
