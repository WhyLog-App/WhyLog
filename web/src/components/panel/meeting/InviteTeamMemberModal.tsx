import { useState } from "react";
import Modal from "@/components/common/Modal";

interface InviteTeamMemberModalProps {
  onClose: () => void;
  onInvite: (email: string) => void;
  isPending?: boolean;
  errorMessage?: string | null;
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const InviteTeamMemberModal = ({
  onClose,
  onInvite,
  isPending = false,
  errorMessage,
}: InviteTeamMemberModalProps) => {
  const [email, setEmail] = useState("");

  const trimmed = email.trim();
  const isValidEmail = EMAIL_REGEX.test(trimmed);

  const handleInvite = () => {
    if (isPending || !isValidEmail) return;
    onInvite(trimmed);
  };

  return (
    <Modal
      title="팀원 초대"
      onClose={onClose}
      primaryLabel={isPending ? "초대 중..." : "초대하기"}
      onPrimaryClick={handleInvite}
      isPrimaryDisabled={!isValidEmail || isPending}
    >
      <div className="flex flex-col gap-2">
        <label
          htmlFor="invite-email"
          className="typo-label text-(--color-text-secondary)"
        >
          초대할 이메일
        </label>
        <input
          id="invite-email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="member@example.com"
          className="typo-body6 h-11 w-full rounded-full border border-white bg-white/50 px-4 py-3 text-(--color-text-primary) placeholder:text-(--color-text-tertiary) focus:outline-none"
        />
        {errorMessage && (
          <p className="typo-body6 text-error-500">{errorMessage}</p>
        )}
      </div>
    </Modal>
  );
};

export default InviteTeamMemberModal;
