import type { HTMLAttributes, ReactNode } from "react";

interface GlassCardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode;
}

const GlassCard = ({ className = "", children, ...rest }: GlassCardProps) => {
  return (
    <div
      className={`flex flex-col rounded-2xl border border-white bg-white/50 backdrop-blur-md ${className}`}
      {...rest}
    >
      {children}
    </div>
  );
};

export default GlassCard;
