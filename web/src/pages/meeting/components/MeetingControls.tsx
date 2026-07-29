import IconHeadphones from "@/assets/icons/media/ic_headphones.svg?react";
import IconHeadphonesOff from "@/assets/icons/media/ic_headphones_off.svg?react";
import IconMic from "@/assets/icons/media/ic_mic.svg?react";
import IconMicOff from "@/assets/icons/media/ic_mic_off.svg?react";
import IconCloseMd from "@/assets/icons/menu/ic_close_md.svg?react";
import { Icon } from "@/components/common/Icon";

interface MeetingControlsProps {
  onEnd: () => void;
  isMicEnabled: boolean;
  isAudioOutputEnabled: boolean;
  onToggleMic: () => void;
  onToggleAudioOutput: () => void;
}

const MeetingControls = ({
  onEnd,
  isMicEnabled,
  isAudioOutputEnabled,
  onToggleMic,
  onToggleAudioOutput,
}: MeetingControlsProps) => {
  const baseBtn =
    "flex h-11 w-11 cursor-pointer items-center justify-center rounded-full shadow-[0px_2px_8px_0px_rgba(40,41,61,0.08)]";
  const enabledBtn = "bg-(--color-bg-surface)";
  const disabledBtn = "bg-(--color-red-500)";
  const enabledIcon = "text-(--color-text-primary)";
  const disabledIcon = "text-(--color-text-inverse)";

  return (
    <div className="flex items-center justify-center gap-4">
      <button
        type="button"
        onClick={onToggleAudioOutput}
        aria-pressed={!isAudioOutputEnabled}
        aria-label={isAudioOutputEnabled ? "듣기 끄기" : "듣기 켜기"}
        className={`${baseBtn} ${isAudioOutputEnabled ? enabledBtn : disabledBtn}`}
      >
        <Icon
          icon={isAudioOutputEnabled ? IconHeadphones : IconHeadphonesOff}
          size={20}
          className={isAudioOutputEnabled ? enabledIcon : disabledIcon}
        />
      </button>
      <button
        type="button"
        onClick={onToggleMic}
        aria-pressed={!isMicEnabled}
        aria-label={isMicEnabled ? "마이크 음소거" : "마이크 켜기"}
        className={`${baseBtn} ${isMicEnabled ? enabledBtn : disabledBtn}`}
      >
        <Icon
          icon={isMicEnabled ? IconMic : IconMicOff}
          size={20}
          className={isMicEnabled ? enabledIcon : disabledIcon}
        />
      </button>
      <button
        type="button"
        onClick={onEnd}
        aria-label="회의 종료"
        className={`${baseBtn} bg-red-500`}
      >
        <Icon
          icon={IconCloseMd}
          size={20}
          className="text-(--color-text-inverse)"
        />
      </button>
    </div>
  );
};

export default MeetingControls;
