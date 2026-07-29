import type { ComponentType, CSSProperties, SVGProps } from "react";
export type IconSource = string | ComponentType<SVGProps<SVGSVGElement>>;

interface IconProps extends Omit<SVGProps<SVGSVGElement>, "width" | "height"> {
  icon: IconSource;
  size?: number;
}

export const Icon = ({
  icon: IconComponent,
  size = 24,
  className,
  ...props
}: IconProps) => {
  if (typeof IconComponent === "string") {
    const maskStyle: CSSProperties = {
      width: size,
      height: size,
      display: "inline-block",
      backgroundColor: "currentColor",
      WebkitMaskImage: `url("${IconComponent}")`,
      maskImage: `url("${IconComponent}")`,
      WebkitMaskRepeat: "no-repeat",
      maskRepeat: "no-repeat",
      WebkitMaskPosition: "center",
      maskPosition: "center",
      WebkitMaskSize: "contain",
      maskSize: "contain",
      flexShrink: 0,
    };

    return <span className={className} style={maskStyle} aria-hidden="true" />;
  }

  return (
    <IconComponent
      width={size}
      height={size}
      className={className}
      aria-hidden="true"
      {...props}
    />
  );
};
