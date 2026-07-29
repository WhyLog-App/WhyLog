import { useNavigate } from "react-router-dom";
import LogoSymbol from "@/components/logo/LogoSymbol";
import LogoText from "@/components/logo/LogoText";
import { ROUTES } from "@/constants/routes";

const Navbar = () => {
  const navigate = useNavigate();
  const goLogin = () => navigate(ROUTES.LOGIN);
  const goSignup = () => navigate(ROUTES.SIGNUP);

  return (
    <header className="fixed top-0 inset-x-0 z-50 nav-glass">
      <div className="max-w-7xl mx-auto h-17 flex items-center justify-between px-4 sm:px-6">
        {/* biome-ignore lint/a11y/useValidAnchor: 홈 링크 자리 — 라우팅 확정 전까지 placeholder */}
        <a href="#" className="flex items-center gap-2.5">
          <LogoSymbol className="h-7" aria-hidden="true" />
          <LogoText className="h-6" aria-hidden="true" />
        </a>

        <nav className="hidden lg:flex items-center gap-9 text-[14px] text-ink-500 font-medium">
          <a href="#features" className="hover:text-ink-900 transition">
            기능
          </a>
          <a href="#how" className="hover:text-ink-900 transition">
            작동 방식
          </a>
          <a href="#tech" className="hover:text-ink-900 transition">
            기술
          </a>
          <a href="#usecases" className="hover:text-ink-900 transition">
            사용 사례
          </a>
          <a href="#faq" className="hover:text-ink-900 transition">
            FAQ
          </a>
        </nav>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={goLogin}
            className="inline-flex h-10 px-4 items-center text-[14px] font-semibold text-ink-700 hover:text-ink-900 transition"
          >
            로그인
          </button>
          <button
            type="button"
            onClick={goSignup}
            className="btn-primary hidden sm:inline-flex h-10 px-5 rounded-full text-[14px] font-semibold items-center gap-1.5"
          >
            무료로 시작하기
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <title>화살표</title>
              <path
                d="M5 12h14m-6-6 6 6-6 6"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
