import {
  FEATURE_1_BULLETS,
  FEATURE_2_BULLETS,
  FEATURE_2_TIMELINE,
  FEATURE_3_BULLETS,
  FEATURE_3_COMMITS,
  WAVEFORM_BARS,
} from "../../constants";
import CountUp from "../primitives/CountUp";
import TiltCard from "../primitives/TiltCard";

const Bullet = ({ text }: { text: string }) => (
  <li className="flex items-center gap-3 text-[14px] text-ink-700">
    <span className="w-1.5 h-1.5 rounded-full bg-primary-500" />
    {text}
  </li>
);

const FeaturesSection = () => (
  <section
    id="features"
    className="relative overflow-hidden grain py-20 md:py-32"
  >
    <div className="relative max-w-7xl mx-auto px-6">
      <div className="text-center mb-16 md:mb-24 reveal">
        <p className="text-[12px] tracking-[0.22em] font-bold text-primary-500 uppercase mb-4">
          FEATURES
        </p>
        <h2 className="text-[40px] md:text-[52px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.1]">
          회의에서 코드까지, <span className="iri-text">한 흐름으로</span>
        </h2>
        <p className="mt-5 text-[18px] text-ink-500 max-w-2xl mx-auto">
          세 단계가 자연스럽게 연결되어, 결정의 맥락을 코드 한 줄까지
          추적합니다.
        </p>
      </div>

      {/* Feature 1 — 회의 자동 분석 */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-12 items-center mb-20 md:mb-32 reveal">
        <div className="col-span-1 lg:col-span-5">
          <span className="num text-[13px] font-semibold text-primary-500 tracking-wide">
            FEATURE 01 · 회의 자동 분석
          </span>
          <h3 className="mt-3 text-[34px] md:text-[40px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.12]">
            AI가 회의를 듣고
            <br />
            핵심을 정리합니다
          </h3>
          <p className="mt-5 text-[16px] leading-[1.75] text-ink-500">
            음성 회의를 진행하면 AI가 회의 맥락을 분석하여 핵심 결정사항을
            자동으로 추출합니다. 논의 주제, 핵심 맥락, 최종 결정, 결정 근거가
            자동 구조화됩니다.
          </p>
          <ul className="mt-8 space-y-3">
            {FEATURE_1_BULLETS.map((t) => (
              <Bullet key={t} text={t} />
            ))}
          </ul>
        </div>

        <div className="col-span-1 lg:col-span-7 relative">
          <div
            className="absolute -inset-6 rounded-[40px] opacity-80"
            style={{
              background:
                "radial-gradient(60% 60% at 30% 50%, rgba(167,139,250,0.20), transparent 65%), radial-gradient(60% 60% at 80% 70%, rgba(122,183,255,0.25), transparent 65%)",
            }}
          />
          <TiltCard>
            <div className="relative glass-strong rounded-[28px] p-5 sm:p-7">
              <div className="flex items-center justify-between mb-5">
                <div className="flex items-center gap-2.5">
                  <div className="w-9 h-9 rounded-xl bg-primary-500 text-white flex items-center justify-center shadow-[0_6px_18px_rgba(30,91,232,0.35)]">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                      <title>마이크</title>
                      <path
                        d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"
                        stroke="white"
                        strokeWidth="1.6"
                      />
                      <path
                        d="M19 12a7 7 0 0 1-14 0M12 19v3"
                        stroke="white"
                        strokeWidth="1.6"
                        strokeLinecap="round"
                      />
                    </svg>
                  </div>
                  <div>
                    <p className="text-[15px] font-bold text-ink-900">
                      서버 개발 팀 4차 회의
                    </p>
                    <p className="text-[12px] text-ink-300 num">
                      2025.01.20 · 47분 · 5명 참여
                    </p>
                  </div>
                </div>
                <span className="num text-[12px] font-semibold text-primary-700 bg-primary-50 px-3 py-1.5 rounded-full">
                  신뢰도 90점
                </span>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="glass-flat rounded-2xl p-4">
                  <p className="text-[11px] font-semibold tracking-wider text-ink-300 uppercase mb-2">
                    논의 주제
                  </p>
                  <p className="text-[13px] font-semibold text-ink-900">
                    Redis 키 정책 변경
                  </p>
                  <p className="text-[12px] text-ink-500 mt-1">
                    세션 유형별 TTL 분리 여부
                  </p>
                </div>
                <div className="glass-flat rounded-2xl p-4">
                  <p className="text-[11px] font-semibold tracking-wider text-ink-300 uppercase mb-2">
                    핵심 맥락
                  </p>
                  <p className="text-[12px] text-ink-700 leading-[1.55]">
                    인증·캐시 세션이 같은 TTL을 공유 → 만료 정책 충돌 우려
                  </p>
                </div>
                <div className="glass-flat rounded-2xl p-4">
                  <p className="text-[11px] font-semibold tracking-wider text-ink-300 uppercase mb-2">
                    최종 결정
                  </p>
                  <p className="text-[13px] font-semibold text-ink-900">
                    Redis 유지 + 키 네이밍 분리
                  </p>
                </div>
                <div className="glass-flat rounded-2xl p-4">
                  <p className="text-[11px] font-semibold tracking-wider text-ink-300 uppercase mb-2">
                    결정 근거
                  </p>
                  <ul className="text-[12px] text-ink-700 space-y-1">
                    <li className="flex gap-1.5">
                      <span className="text-primary-500">✓</span> 운영 복잡도
                      우려
                    </li>
                    <li className="flex gap-1.5">
                      <span className="text-primary-500">✓</span> Mongo 이전
                      비용 부담
                    </li>
                  </ul>
                </div>
              </div>

              <div className="mt-5 flex items-center gap-1.5">
                <span className="text-[10px] num font-semibold text-ink-300 mr-1">
                  12:24
                </span>
                <div className="flex-1 flex items-center gap-0.75 h-8">
                  {WAVEFORM_BARS.map((h, i) => (
                    <span
                      // biome-ignore lint/suspicious/noArrayIndexKey: static waveform
                      key={i}
                      className={`block w-0.75 rounded-full ${
                        h >= 80 ? "bg-primary-500" : "bg-primary-300"
                      }`}
                      style={{ height: `${h}%` }}
                    />
                  ))}
                </div>
                <span className="text-[10px] num font-semibold text-ink-300 ml-1">
                  12:47
                </span>
              </div>
            </div>
          </TiltCard>
          <div className="absolute -top-8 -right-6 iri-morph w-22 h-22 float-a" />
        </div>
      </div>

      {/* Feature 2 — 의사결정 기록 */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-12 items-center mb-20 md:mb-32 reveal">
        <div className="col-span-1 lg:col-span-7 order-2 lg:order-1 relative">
          <div
            className="absolute -inset-6 rounded-[40px] opacity-80"
            style={{
              background:
                "radial-gradient(60% 60% at 80% 40%, rgba(240,171,252,0.20), transparent 65%), radial-gradient(60% 60% at 20% 80%, rgba(167,139,250,0.22), transparent 65%)",
            }}
          />
          <TiltCard>
            <div className="relative glass-strong rounded-[28px] p-5 sm:p-7">
              <div className="flex items-center justify-between mb-5">
                <div>
                  <p className="text-[20px] font-extrabold text-ink-900 tracking-tightish">
                    Redis 키 정책 변경
                  </p>
                  <p className="text-[12px] text-ink-300 num mt-0.5">
                    서버 개발 팀 4차 회의 · 2025.01.20
                  </p>
                </div>
                <span className="num text-[11px] font-bold text-primary-700 bg-primary-50 px-2.5 py-1 rounded-md">
                  신뢰도 90점
                </span>
              </div>

              <div className="glass-flat rounded-2xl p-4 mb-3">
                <p className="text-[11px] font-semibold tracking-wider text-ink-300 uppercase mb-3">
                  ⏱ 타임라인
                </p>
                <div className="space-y-2.5">
                  {FEATURE_2_TIMELINE.map((row) => (
                    <div key={row.t} className="flex items-center gap-3">
                      <span className="num text-[11px] text-ink-300 w-9">
                        {row.t}
                      </span>
                      <span className="w-1.5 h-1.5 rounded-full bg-primary-500" />
                      <span
                        className={`text-[13px] ${
                          row.strong
                            ? "font-semibold text-ink-900"
                            : "text-ink-700"
                        }`}
                      >
                        {row.label}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="glass-flat rounded-2xl p-4">
                <p className="text-[11px] font-semibold tracking-wider text-ink-300 uppercase mb-3">
                  결정 원문 맥락
                </p>
                <div className="space-y-3">
                  <div className="flex gap-3">
                    <div className="w-7 h-7 rounded-full bg-linear-to-br from-primary-300 to-primary-500 shrink-0" />
                    <div>
                      <p className="text-[12px] font-semibold text-ink-900">
                        김준용{" "}
                        <span className="num text-ink-300 font-normal">
                          12:24
                        </span>
                      </p>
                      <p className="text-[12.5px] text-ink-700 leading-[1.6] mt-0.5">
                        Redis 키 만료 정책이 지금 TTL 고정인데, 세션 유형별로
                        다르게 가져가야 할 것 같아요.
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <div
                      className="w-7 h-7 rounded-full shrink-0"
                      style={{
                        background: "linear-gradient(135deg, #a78bfa, #f0abfc)",
                      }}
                    />
                    <div>
                      <p className="text-[12px] font-semibold text-ink-900">
                        유진{" "}
                        <span className="num text-ink-300 font-normal">
                          12:26
                        </span>
                      </p>
                      <p className="text-[12.5px] text-ink-700 leading-[1.6] mt-0.5">
                        Mongo로 옮기자는 의견 있었는데, 운영 복잡도 올라가니까
                        Redis 유지하면서 키 네이밍만 바꾸는 게 낫지 않을까요?
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </TiltCard>
          <div className="absolute -bottom-8 -left-6 iri-sphere w-17 h-17 float-c" />
        </div>

        <div className="col-span-1 lg:col-span-5 order-1 lg:order-2">
          <span className="num text-[13px] font-semibold text-primary-500 tracking-wide">
            FEATURE 02 · 의사결정 기록
          </span>
          <h3 className="mt-3 text-[34px] md:text-[40px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.12]">
            결정의 근거와 맥락을
            <br />
            그대로 보존
          </h3>
          <p className="mt-5 text-[16px] leading-[1.75] text-ink-500">
            누가, 언제, 왜 그렇게 결정했는지를 타임라인 · 결정근거 · 원문
            맥락으로 함께 저장합니다. 신뢰도 점수와 함께요.
          </p>
          <ul className="mt-8 space-y-3">
            {FEATURE_2_BULLETS.map((t) => (
              <Bullet key={t} text={t} />
            ))}
          </ul>
        </div>
      </div>

      {/* Feature 3 — Git 커밋 매칭 */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-12 items-center reveal">
        <div className="col-span-1 lg:col-span-5">
          <span className="num text-[13px] font-semibold text-primary-500 tracking-wide">
            FEATURE 03 · Git 커밋 매칭
          </span>
          <h3 className="mt-3 text-[34px] md:text-[40px] font-extrabold tracking-tighter2 text-ink-900 leading-[1.12]">
            결정과 코드를
            <br />
            잇는 다리
          </h3>
          <p className="mt-5 text-[16px] leading-[1.75] text-ink-500">
            GitHub 레포지토리를 연동하면 커밋 이력을 수집하고, 회의에서 나온
            결정사항과 자동으로 매칭합니다. 추천 사유까지 함께 제공돼 한 번에
            맥락이 이어집니다.
          </p>
          <ul className="mt-8 space-y-3">
            {FEATURE_3_BULLETS.map((t) => (
              <Bullet key={t} text={t} />
            ))}
          </ul>
        </div>

        <div className="col-span-1 lg:col-span-7 relative">
          <div
            className="absolute -inset-6 rounded-[40px] opacity-80"
            style={{
              background:
                "radial-gradient(60% 60% at 30% 60%, rgba(122,183,255,0.25), transparent 65%), radial-gradient(60% 60% at 80% 30%, rgba(167,139,250,0.20), transparent 65%)",
            }}
          />
          <TiltCard>
            <div className="relative glass-strong rounded-[28px] p-5 sm:p-7">
              <div className="flex items-center justify-between mb-5">
                <p className="text-[15px] font-bold text-ink-900">
                  커밋 추천 결과
                </p>
                <div className="flex items-center gap-1.5">
                  <span className="num text-[11px] font-bold text-primary-700 bg-primary-50 px-2.5 py-1 rounded-md">
                    추천 커밋
                  </span>
                  <span className="num text-[11px] font-medium text-ink-500 bg-white border border-line-soft px-2.5 py-1 rounded-md">
                    연결됨
                  </span>
                </div>
              </div>

              <div className="rounded-2xl border border-line-soft overflow-x-auto bg-white/60">
                <div className="min-w-105">
                  <div className="grid grid-cols-[1fr_84px_1.6fr_72px_60px] px-4 py-2.5 text-[10.5px] tracking-wider font-semibold text-ink-300 uppercase border-b border-line-soft bg-white/50">
                    <span>Repository</span>
                    <span>Commit</span>
                    <span>Message</span>
                    <span>사유</span>
                    <span className="text-right">연결</span>
                  </div>
                  {FEATURE_3_COMMITS.map((row, idx, arr) => (
                    <div
                      key={row.sha}
                      className={`grid grid-cols-[1fr_84px_1.6fr_72px_60px] px-4 py-3 text-[13px] items-center hover:bg-primary-50/30 transition ${
                        idx < arr.length - 1 ? "border-b border-line-faint" : ""
                      }`}
                    >
                      <span className="font-semibold text-ink-900">
                        {row.repo}
                      </span>
                      <span className="num text-[11px] font-bold text-primary-700 bg-primary-50 px-2 py-0.5 rounded inline-block w-fit">
                        {row.sha}
                      </span>
                      <span className="text-ink-700 truncate">{row.msg}</span>
                      <span
                        aria-hidden="true"
                        className="text-primary-500 font-semibold text-[12px] text-left"
                      >
                        사유 보기
                      </span>
                      <span className="text-right">
                        <span
                          aria-hidden="true"
                          className="inline-block text-[11px] font-semibold text-white bg-primary-500 px-2.5 py-1 rounded-md"
                        >
                          연결
                        </span>
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="mt-4 grid grid-cols-3 gap-2.5">
                <div className="glass-flat rounded-xl px-3 py-2.5">
                  <p className="text-[10.5px] tracking-wider font-semibold text-ink-300 uppercase">
                    근거 발견
                  </p>
                  <p className="num text-[15px] font-extrabold text-ink-900 mt-1">
                    <CountUp to={42} duration={1400} />
                    <span className="text-[11px] text-ink-500 font-medium ml-0.5">
                      개
                    </span>
                  </p>
                </div>
                <div className="glass-flat rounded-xl px-3 py-2.5">
                  <p className="text-[10.5px] tracking-wider font-semibold text-ink-300 uppercase">
                    참여자 합의
                  </p>
                  <p className="text-[13px] font-bold text-primary-500 mt-1">
                    높음
                  </p>
                </div>
                <div className="glass-flat rounded-xl px-3 py-2.5">
                  <p className="text-[10.5px] tracking-wider font-semibold text-ink-300 uppercase">
                    결정 ↔ 구현
                  </p>
                  <p className="num text-[15px] font-extrabold text-ink-900 mt-1">
                    <CountUp to={92} duration={1500} suffix="" />
                    <span className="text-[11px] text-ink-500 font-medium ml-0.5">
                      %
                    </span>
                  </p>
                </div>
              </div>
            </div>
          </TiltCard>
          <div className="absolute -top-6 -right-6 iri-sphere w-18 h-18 float-b" />
        </div>
      </div>
    </div>
  </section>
);

export default FeaturesSection;
