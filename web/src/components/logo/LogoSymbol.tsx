import { type SVGProps, useId } from "react";

function LogoSymbol(props: SVGProps<SVGSVGElement>) {
  const gradientId = useId();

  return (
    <svg viewBox="0 0 76 65.143" fill="none" {...props}>
      <title>WhyLog Logo</title>
      <defs>
        <linearGradient
          id={gradientId}
          x1="1.80616"
          y1="32.5714"
          x2="48.6847"
          y2="10.3401"
          gradientUnits="userSpaceOnUse"
        >
          <stop offset="0.221154" stopColor="#1E5BE8" />
          <stop offset="0.822115" stopColor="#589EFA" />
        </linearGradient>
      </defs>
      <circle
        cx="17.5782"
        cy="32.5682"
        r="17.0782"
        stroke="#589EFA"
        strokeOpacity="0.5"
      />
      <circle cx="17.5762" cy="32.5702" r="12.4082" fill="#1E5BE8" />
      <g transform="translate(16.38, 0)">
        <circle cx="49.2863" cy="10.3401" r="10.3401" fill="#589EFA" />
        <path
          d="M1.20463 32.5714C8.38925 23.7127 25.1902 9.096 49.2863 10.4248"
          stroke={`url(#${gradientId})`}
          strokeWidth="3.10204"
          strokeDasharray="3.5 3.5"
        />
      </g>
      <g transform="translate(16.38, 31.595) scale(1, -1) translate(0, -33.5483)">
        <circle cx="49.2863" cy="10.3401" r="10.3401" fill="#589EFA" />
        <path
          d="M1.20463 32.5714C8.38925 23.7127 25.1902 9.096 49.2863 10.4248"
          stroke={`url(#${gradientId})`}
          strokeWidth="3.10204"
          strokeDasharray="3.5 3.5"
        />
      </g>
    </svg>
  );
}

export default LogoSymbol;
