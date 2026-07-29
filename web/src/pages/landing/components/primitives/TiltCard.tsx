import {
  memo,
  type MouseEvent as ReactMouseEvent,
  type ReactNode,
  useRef,
} from "react";

type Props = {
  children: ReactNode;
  max?: number;
  className?: string;
};

/**
 * 3D perspective tilt — 마우스 위치에 따라 카드가 살짝 기울어진다.
 * DOM transform 을 직접 조작 (state 미사용) 해 리렌더를 발생시키지 않는다.
 */
const TiltCard = ({ children, max = 6, className }: Props) => {
  const ref = useRef<HTMLDivElement>(null);
  const onMove = (e: ReactMouseEvent<HTMLDivElement>) => {
    const el = ref.current;
    if (!el) return;
    const r = el.getBoundingClientRect();
    const px = (e.clientX - r.left) / r.width - 0.5;
    const py = (e.clientY - r.top) / r.height - 0.5;
    el.style.transform = `perspective(1000px) rotateY(${px * max}deg) rotateX(${-py * max}deg)`;
  };
  const onLeave = () => {
    const el = ref.current;
    if (el) el.style.transform = "";
  };
  return (
    // biome-ignore lint/a11y/noStaticElementInteractions: 장식용 3D tilt — 키보드/스크린리더 동작에 영향 없음
    <div
      ref={ref}
      onMouseMove={onMove}
      onMouseLeave={onLeave}
      className={`tilt ${className ?? ""}`}
      role="presentation"
    >
      {children}
    </div>
  );
};

export default memo(TiltCard);
