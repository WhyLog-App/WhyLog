import { TECH_ACCURACY_BARS } from "../../constants";
import CountUp from "../primitives/CountUp";

const TechSection = () => (
  <section id="tech" className="relative overflow-hidden grain py-20 md:py-32">
    <div className="absolute right-[5%] top-[12%] iri-sphere w-32 h-32 float-a opacity-90" />
    <div className="absolute left-[8%] bottom-[14%] iri-sphere w-24 h-24 float-b" />
    <div className="absolute right-[18%] bottom-[8%] iri-sphere w-16 h-16 float-c" />
    <div className="absolute left-[42%] top-[8%] iri-sphere w-12 h-12 float-a opacity-80" />
    <div className="absolute right-[40%] bottom-[20%] iri-sphere w-10 h-10 float-c opacity-90" />

    <div className="relative max-w-7xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-12 items-center">
      <div className="col-span-1 lg:col-span-6 reveal">
        <p className="text-[12px] tracking-[0.22em] font-bold text-primary-500 uppercase mb-4">
          TECHNOLOGY
        </p>
        <h2 className="text-[32px] sm:text-[38px] md:text-[48px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.1]">
          임베딩 검색 기반으로
          <br />
          관련 커밋을 추천합니다
        </h2>
        <p className="mt-6 text-[17px] leading-[1.75] text-ink-500 max-w-lg">
          회의 적용사항과 커밋 요약을 벡터화해 후보를 찾고, 키워드·파일
          맥락·커밋 타입 점수로 재정렬해 신뢰도 높은 커밋만 추천합니다.
        </p>

        <div className="mt-9 glass rounded-2xl p-5 max-w-lg">
          <p className="text-[10.5px] tracking-wider font-semibold text-ink-300 uppercase mb-2">
            Scoring Formula
          </p>
          <div className="mono text-[15.5px] text-ink-900 leading-[1.7] flex flex-wrap items-center gap-x-1.5">
            <span className="text-ink-700">score</span>
            <span className="text-ink-300">=</span>
            <span className="text-primary-500 font-bold">semantic</span>
            <span className="text-ink-300">+</span>
            <span className="text-primary-500 font-bold">keyword</span>
            <span className="text-ink-300">+</span>
            <span className="text-primary-500 font-bold">context</span>
            <span className="text-ink-300">+</span>
            <span className="text-primary-500 font-bold">type</span>
            <span className="text-ink-300">−</span>
            <span className="text-accent-lavender font-bold">penalty</span>
          </div>
          <div className="mt-4 flex flex-wrap items-center gap-x-3 gap-y-2 text-[12px] text-ink-500">
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-primary-500" />
              semantic (임베딩 의미 검색)
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-primary-500" />
              keyword (키워드 일치)
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-primary-500" />
              context (파일 맥락)
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-primary-500" />
              type (커밋 타입)
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-accent-lavender" />
              penalty (모호·반대 의미)
            </span>
          </div>
        </div>
      </div>

      <div className="col-span-1 lg:col-span-6 reveal">
        <div className="space-y-4">
          <div className="glass card-lift rounded-3xl p-5 sm:p-7 flex items-center gap-4 sm:gap-7">
            <div className="shrink-0">
              <p className="num text-[48px] sm:text-[64px] font-extrabold text-primary-500 leading-none tracking-tighter2">
                <CountUp to={80} duration={1700} />
                <span className="text-[36px] text-primary-300">%</span>
              </p>
            </div>
            <div className="flex-1">
              <p className="text-[16px] font-bold text-ink-900">
                1위 매칭 정확도
              </p>
              <p className="text-[13.5px] text-ink-500 mt-1.5 leading-relaxed">
                결정사항과 가장 관련된 커밋이 추천 결과 최상단에 노출되는 비율
              </p>
            </div>
            <div className="w-20 h-12 hidden sm:flex items-end gap-1">
              {TECH_ACCURACY_BARS.map((b) => (
                <span
                  key={b.id}
                  className={`block w-2 rounded ${b.cls}`}
                  style={{ height: `${b.h}%` }}
                />
              ))}
            </div>
          </div>

          <div className="glass card-lift rounded-3xl p-5 sm:p-7 flex items-center gap-4 sm:gap-7">
            <div className="shrink-0">
              <p className="num text-[48px] sm:text-[64px] font-extrabold leading-none tracking-tighter2 iri-text">
                <CountUp to={99} duration={1900} />
                <span className="text-[36px]">%</span>
              </p>
            </div>
            <div className="flex-1">
              <p className="text-[16px] font-bold text-ink-900">
                상위 5위 내 정답 포함율
              </p>
              <p className="text-[13.5px] text-ink-500 mt-1.5 leading-relaxed">
                관련 커밋이 상위 5개 추천 안에 반드시 포함되는 비율
              </p>
            </div>
            <div className="hidden sm:flex flex-col gap-1.5 w-20">
              {["bar-a", "bar-b", "bar-c"].map((k) => (
                <div
                  key={k}
                  className="h-2 rounded-full bg-line-soft overflow-hidden"
                >
                  <div
                    className="h-full"
                    style={{
                      width: "100%",
                      background: "linear-gradient(to right, #1e5be8, #a78bfa)",
                    }}
                  />
                </div>
              ))}
            </div>
          </div>

          <div className="glass card-lift rounded-3xl p-5 sm:p-7 flex items-center gap-4 sm:gap-7">
            <div className="shrink-0">
              <p className="num text-[40px] sm:text-[52px] font-extrabold text-primary-500 leading-none tracking-tighter2">
                0.5<span className="text-ink-300">–</span>1.8
                <span className="text-[28px] text-primary-300 ml-1">s</span>
              </p>
            </div>
            <div className="flex-1">
              <p className="text-[16px] font-bold text-ink-900">
                평균 검색 속도
              </p>
              <p className="text-[13.5px] text-ink-500 mt-1.5 leading-relaxed">
                결정사항 입력부터 추천 커밋 반환까지의 응답 시간
              </p>
            </div>
            <div className="hidden sm:block w-20">
              <svg viewBox="0 0 80 40" className="w-full">
                <title>속도 그래프</title>
                <path
                  d="M0,30 Q20,5 40,15 T80,12"
                  stroke="#1E5BE8"
                  strokeWidth="2"
                  fill="none"
                  strokeLinecap="round"
                />
                <circle cx="80" cy="12" r="3" fill="#1E5BE8" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
);

export default TechSection;
