import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";

interface ParticipantTileProps {
  name: string;
  isSelf?: boolean;
  profileImage?: string | null;
}

const ParticipantTile = ({
  name,
  isSelf,
  profileImage,
}: ParticipantTileProps) => {
  return (
    <div
      className={`relative flex aspect-16/10 w-full items-center justify-center overflow-hidden rounded-2xl bg-(--color-dark-700) ${
        isSelf ? "border-2 border-green-500" : ""
      }`}
    >
      {profileImage ? (
        <img
          src={profileImage}
          alt={name}
          className="absolute inset-0 h-full w-full object-cover"
        />
      ) : (
        <Icon
          icon={IconCircleUser}
          size={64}
          className="text-(--color-dark-100)"
        />
      )}
      <span className="typo-body6 absolute bottom-3 left-3 rounded-md bg-black-60 px-2 py-1 text-(--color-text-inverse)">
        {name}
      </span>
    </div>
  );
};

export default ParticipantTile;
