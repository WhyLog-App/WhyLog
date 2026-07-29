import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/constants/routes";

const FinalCtaSection = () => {
  const navigate = useNavigate();
  const goSignup = () => navigate(ROUTES.SIGNUP);

  return (
    <section className="relative overflow-hidden grain py-24 md:py-36 mesh-bg-strong alive">
      <div className="relative max-w-4xl mx-auto px-6 text-center reveal">
        <div className="relative mx-auto mb-10 w-32.5 h-32.5 sm:w-45 sm:h-45">
          <div
            className="absolute -inset-12 rounded-full opacity-70"
            style={{
              background:
                "radial-gradient(closest-side, rgba(167,139,250,0.55), transparent 70%)",
              filter: "blur(28px)",
            }}
          />
          <div className="iri-sphere relative w-full h-full float-a" />
        </div>

        <h2 className="text-[34px] sm:text-[44px] md:text-[64px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.05]">
          지금, 첫 결정을 <span className="why-grad">기록해보세요</span>
        </h2>
        <p className="mt-6 text-[18px] text-ink-500 max-w-xl mx-auto leading-[1.65]">
          5분이면 팀 셋업이 끝납니다. 카드 등록 없이 무료로 체험해보세요.
        </p>
        <div className="mt-10 flex flex-wrap items-center justify-center gap-3">
          <button
            type="button"
            onClick={goSignup}
            className="btn-primary h-14 px-8 rounded-full text-[15px] font-semibold inline-flex items-center gap-2"
          >
            무료로 시작하기
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <title>화살표</title>
              <path
                d="M5 12h14m-6-6 6 6-6 6"
                stroke="currentColor"
                strokeWidth="2.2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
          <button
            type="button"
            disabled
            title="준비중"
            className="btn-ghost h-14 px-7 rounded-full text-[15px] font-semibold opacity-60 cursor-not-allowed"
          >
            팀 데모 요청하기 (준비중)
          </button>
        </div>
      </div>
    </section>
  );
};

export default FinalCtaSection;
