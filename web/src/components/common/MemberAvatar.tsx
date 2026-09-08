import { useState } from "react";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";

interface MemberAvatarProps {
  src?: string | null;
  alt?: string;
  size?: number;
  className?: string;
}

/**
 * 멤버 프로필 아바타.
 * - src가 있고 로드에 성공하면 이미지를 표시
 * - 없거나 로드 실패 시 기본 아이콘으로 폴백
 */
const MemberAvatar = ({
  src,
  alt = "프로필 이미지",
  size = 32,
  className = "",
}: MemberAvatarProps) => {
  const [failedSrc, setFailedSrc] = useState<string | null>(null);
  const imageSrc = src && src !== failedSrc ? src : null;

  return (
    <span
      className={`flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-transparent ${className}`}
      style={{ width: size, height: size }}
    >
      {imageSrc ? (
        <img
          src={imageSrc}
          alt={alt}
          className="size-full object-cover"
          loading="lazy"
          onError={() => setFailedSrc(src ?? null)}
        />
      ) : (
        <Icon
          icon={IconCircleUser}
          size={Math.round(size * 0.75)}
          className="text-(--color-dark-100)"
        />
      )}
    </span>
  );
};

export default MemberAvatar;
