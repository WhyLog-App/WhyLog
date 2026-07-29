import { useEffect, useState } from "react";

interface TeamImageProps {
  src: string | null | undefined;
  alt: string;
  size: number;
  className?: string;
}

export const TeamImage = ({
  src,
  alt,
  size,
  className = "",
}: TeamImageProps) => {
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    setImageError(false);
  }, []);

  const showFallback = !src || imageError;

  if (showFallback) {
    return (
      <div
        className={`shrink-0 rounded-sm bg-gray-400 ${className}`}
        style={{ width: `${size}px`, height: `${size}px` }}
        role="img"
        aria-label={alt}
      />
    );
  }

  return (
    <img
      src={src}
      alt={alt}
      className={`shrink-0 rounded-sm object-cover ${className}`}
      style={{ width: `${size}px`, height: `${size}px` }}
      onError={() => setImageError(true)}
      loading="lazy"
    />
  );
};
