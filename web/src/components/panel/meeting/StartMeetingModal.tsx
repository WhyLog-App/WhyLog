import { useState } from "react";
import Modal from "@/components/common/Modal";

interface StartMeetingModalProps {
  onClose: () => void;
  onStart: (meetingName: string) => void;
  isPending?: boolean;
}

const StartMeetingModal = ({
  onClose,
  onStart,
  isPending = false,
}: StartMeetingModalProps) => {
  const [meetingName, setMeetingName] = useState("");

  const handleStart = () => {
    if (isPending) return;
    if (meetingName.trim()) {
      onStart(meetingName.trim());
    }
  };

  return (
    <Modal
      title="회의 시작하기"
      onClose={onClose}
      primaryLabel={isPending ? "생성 중..." : "시작하기"}
      onPrimaryClick={handleStart}
      isPrimaryDisabled={isPending || !meetingName.trim()}
    >
      <div className="flex flex-col gap-1">
        <label
          htmlFor="meeting-name"
          className="typo-label text-(--color-text-secondary)"
        >
          회의 이름
        </label>
        <input
          id="meeting-name"
          type="text"
          value={meetingName}
          onChange={(e) => setMeetingName(e.target.value)}
          placeholder="회의 이름을 입력하세요"
          className="typo-body6 h-11 w-full rounded-full border border-white bg-white/50 px-4 py-3 text-(--color-text-primary) placeholder:text-(--color-text-tertiary) focus:outline-none"
        />
      </div>
    </Modal>
  );
};

export default StartMeetingModal;
