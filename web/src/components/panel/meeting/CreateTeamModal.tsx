import { useEffect, useRef, useState } from "react";
import IconCamera from "@/assets/icons/media/ic_camera.svg?react";
import { Icon } from "@/components/common/Icon";
import Modal from "@/components/common/Modal";

interface CreateTeamModalProps {
  onClose: () => void;
  onCreate: (teamName: string, photo: File | null) => void;
  isPending?: boolean;
}

const CreateTeamModal = ({
  onClose,
  onCreate,
  isPending = false,
}: CreateTeamModalProps) => {
  const [teamName, setTeamName] = useState("");
  const [photoFile, setPhotoFile] = useState<File | null>(null);
  const [photoPreview, setPhotoPreview] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    return () => {
      if (photoPreview) URL.revokeObjectURL(photoPreview);
    };
  }, [photoPreview]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] ?? null;

    if (file) {
      setPhotoFile(file);
      setPhotoPreview(URL.createObjectURL(file));
    } else {
      setPhotoFile(null);
      setPhotoPreview(null);
    }
  };

  const handleCreate = () => {
    if (isPending) return;
    if (teamName.trim()) {
      onCreate(teamName.trim(), photoFile);
    }
  };

  return (
    <Modal
      title="새 팀 만들기"
      onClose={onClose}
      primaryLabel={isPending ? "생성 중..." : "만들기"}
      onPrimaryClick={handleCreate}
      isPrimaryDisabled={!teamName.trim() || isPending}
    >
      <div className="flex flex-col gap-6">
        {/* Photo Upload */}
        <div className="flex flex-col gap-2.5">
          <span className="typo-label text-(--color-text-secondary)">
            팀 사진
          </span>
          <div className="flex items-center gap-3">
            <button
              type="button"
              className="flex size-15 shrink-0 cursor-pointer items-center justify-center overflow-hidden rounded-full border border-(--color-border-default) bg-white/50"
              onClick={() => fileInputRef.current?.click()}
              aria-label="팀 사진 업로드"
            >
              {photoPreview ? (
                <img
                  src={photoPreview}
                  alt="팀 사진 미리보기"
                  className="size-full object-cover"
                />
              ) : (
                <Icon
                  icon={IconCamera}
                  size={24}
                  className="text-(--color-text-tertiary)"
                />
              )}
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleFileChange}
              aria-label="팀 사진 파일 선택"
            />
            <span className="typo-body6 text-(--color-text-tertiary)">
              클릭하여 팀 사진을 업로드하세요
            </span>
          </div>
        </div>

        {/* Team Name Input */}
        <div className="flex flex-col gap-1">
          <label
            htmlFor="team-name"
            className="typo-label text-(--color-text-secondary)"
          >
            팀 이름
          </label>
          <input
            id="team-name"
            type="text"
            value={teamName}
            onChange={(e) => setTeamName(e.target.value)}
            placeholder="팀 이름을 입력하세요"
            className="typo-body6 h-11 w-full rounded-full border border-white bg-white/50 px-4 py-3 text-(--color-text-primary) placeholder:text-(--color-text-tertiary) focus:outline-none"
          />
        </div>
      </div>
    </Modal>
  );
};

export default CreateTeamModal;
