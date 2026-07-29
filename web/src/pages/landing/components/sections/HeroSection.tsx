import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/constants/routes";
import {
  HERO_IMAGE,
  HERO_STAR_COUNT,
  HERO_TYPEWRITER_SPEED,
  HERO_TYPEWRITER_START_DELAY,
} from "../../constants";
import Starfield from "../primitives/Starfield";
import Typewriter from "../primitives/Typewriter";

const HeroSection = () => {
  const navigate = useNavigate();
  const goSignup = () => navigate(ROUTES.SIGNUP);

  return (
    <section className="relative overflow-hidden grain mesh-bg alive hero-spotlight pt-28 pb-24 md:pt-30 md:pb-32">
      <Starfield count={HERO_STAR_COUNT} />

      <div
        data-parallax="-0.12"
        className="absolute inset-0 pointer-events-none"
        style={{ transform: "translate3d(0, var(--py, 0px), 0)" }}
      >
        <img
          src={HERO_IMAGE}
          alt=""
          className="absolute -right-[12%] -top-[6%] w-205 max-w-[80vw] opacity-90 pointer-events-none float-a select-none"
          style={{
            mixBlendMode: "multiply",
            filter: "saturate(108%) contrast(102%)",
          }}
        />
      </div>

      <div
        data-parallax="0.18"
        className="absolute inset-0 pointer-events-none"
        style={{ transform: "translate3d(0, var(--py, 0px), 0)" }}
      >
        <div
          className="absolute -left-32 top-1/3 w-130 h-130 rounded-full opacity-60 pointer-events-none float-b"
          style={{
            background:
              "radial-gradient(closest-side, rgba(167,139,250,0.45), transparent 70%)",
            filter: "blur(20px)",
          }}
        />
      </div>

      <div className="relative max-w-7xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
        <div className="col-span-1 lg:col-span-7 reveal">
          <div className="chip mb-7">
            <span className="iri-morph w-3.5 h-3.5" aria-hidden="true" />
            AI가 회의의 맥락을 코드까지 잇습니다
          </div>

          <h1 className="font-extrabold tracking-tighter2 text-[44px] sm:text-[60px] md:text-[84px] lg:text-[96px] leading-[1.02] text-ink-900">
            <Typewriter
              segments={[
                { text: "코드 변경의" },
                { br: true },
                { text: '"Why"', gradient: true },
                { text: "를" },
                { br: true },
                { text: "잊지 않도록." },
              ]}
              speed={HERO_TYPEWRITER_SPEED}
              startDelay={HERO_TYPEWRITER_START_DELAY}
            />
          </h1>

          <p className="mt-7 max-w-140 text-[17px] md:text-[19px] leading-[1.65] text-ink-500">
            회의에서 커밋까지,
            <br className="hidden md:block" />
            의사결정 맥락을 추적하는 AI 협업 플랫폼
          </p>

          <div className="mt-10 flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={goSignup}
              className="btn-primary h-13 px-7 rounded-full text-[15px] font-semibold inline-flex items-center gap-2"
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
              className="btn-ghost h-13 px-6 rounded-full text-[15px] font-semibold inline-flex items-center gap-2.5 opacity-60 cursor-not-allowed"
            >
              <span
                aria-hidden="true"
                className="w-7 h-7 rounded-full bg-primary-50 inline-flex items-center justify-center"
              >
                <svg width="11" height="11" viewBox="0 0 24 24" fill="#1E5BE8">
                  <title>재생</title>
                  <path d="M8 5v14l11-7z" />
                </svg>
              </span>
              데모 영상 보기 (준비중)
            </button>
          </div>
        </div>

        <div className="col-span-1 lg:col-span-5 relative h-105 lg:h-140 hidden lg:block" />
      </div>
    </section>
  );
};

export default HeroSection;
