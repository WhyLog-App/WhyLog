import type { ChangeEvent, FormEvent } from "react";
import { useEffect, useRef, useState } from "react";
import IconTrash from "@/assets/icons/edit/ic_trash.svg?react";
import iconCamera from "@/assets/icons/media/ic_camera.svg";
import GlassCard from "@/components/common/GlassCard";
import { Icon } from "@/components/common/Icon";
import MemberAvatar from "@/components/common/MemberAvatar";
import { ValidationMessage } from "@/components/common/ValidationMessage";
import { MEMBER_NAME_MAX_LENGTH } from "@/constants/member";
import { useProfileImageActions } from "@/hooks/useProfileImageActions";
import type { MyInfo } from "@/types/member";
import {
  PROFILE_IMAGE_ACCEPT,
  validateProfileImageFile,
} from "@/utils/profileImage";
import { useUpdateMyName } from "../hooks/useUpdateMyName";
import { useUpdateProfileVisibility } from "../hooks/useUpdateProfileVisibility";

interface ProfileEditSectionProps {
  info: MyInfo;
  inputClassName: string;
}

export const ProfileEditSection = ({
  info,
  inputClassName,
}: ProfileEditSectionProps) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [name, setName] = useState(info.name);
  const [profilePreviewUrl, setProfilePreviewUrl] = useState<string | null>(
    null,
  );
  const [profileFileError, setProfileFileError] = useState<string | null>(null);
  const nameMutation = useUpdateMyName(info.member_id);
  const imageActions = useProfileImageActions(info.member_id);
  const profileVisibilityMutation = useUpdateProfileVisibility(info.member_id);
  const isProfilePublic = info.profile_visibility === "PUBLIC";

  useEffect(() => {
    setName(info.name);
  }, [info.name]);

  useEffect(
    () => () => {
      if (profilePreviewUrl) URL.revokeObjectURL(profilePreviewUrl);
    },
    [profilePreviewUrl],
  );

  const handleImageChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;
    event.target.value = "";
    if (!file) return;
    const profileImageError = validateProfileImageFile(file);
    if (profileImageError) {
      setProfileFileError(profileImageError);
      return;
    }
    setProfileFileError(null);
    const previewUrl = URL.createObjectURL(file);
    setProfilePreviewUrl(previewUrl);
    await imageActions.uploadImage(file);
    setProfilePreviewUrl(null);
  };

  const handleImageRemove = () => {
    setProfilePreviewUrl(null);
    void imageActions.removeImage();
  };

  const handleNameSubmit = (event: FormEvent) => {
    event.preventDefault();
    const trimmed = name.trim();
    if (!trimmed || trimmed === info.name) return;
    if (trimmed.length > MEMBER_NAME_MAX_LENGTH) {
      nameMutation.setClientError("이름은 50자 이하로 입력해 주세요.");
      return;
    }
    nameMutation.updateName(trimmed);
  };

  const handleProfileVisibilityToggle = () => {
    if (profileVisibilityMutation.isPending) return;
    profileVisibilityMutation.update(isProfilePublic ? "PRIVATE" : "PUBLIC");
  };

  return (
    <section className="grid min-w-0 grid-rows-[auto_1fr] gap-4">
      <div className="lg:min-h-14">
        <h2 className="typo-h5 text-(--color-text-primary)">프로필</h2>
        <p className="typo-body6 text-(--color-text-tertiary)">
          이름과 프로필 이미지를 관리합니다.
        </p>
      </div>
      <GlassCard className="h-full gap-5 p-5 sm:p-6">
        <div className="grid flex-1 content-center gap-x-4 gap-y-3 sm:grid-cols-[auto_minmax(0,1fr)] sm:items-center">
          <div className="flex items-center sm:col-start-1 sm:row-start-1">
            <div className="relative w-fit">
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={imageActions.isPending}
                className="group relative w-fit cursor-pointer rounded-full disabled:cursor-not-allowed disabled:opacity-60"
                aria-label="프로필 이미지 변경"
              >
                <MemberAvatar
                  src={profilePreviewUrl ?? info.profile_image}
                  size={112}
                />
                <span className="absolute inset-0 flex items-center justify-center rounded-full bg-black/30 opacity-0 transition-opacity group-hover:opacity-100 group-focus-visible:opacity-100">
                  <Icon icon={iconCamera} size={18} className="text-white" />
                </span>
              </button>
              {(profilePreviewUrl || info.profile_image) && (
                <button
                  type="button"
                  onClick={handleImageRemove}
                  disabled={imageActions.isPending}
                  className="absolute right-0 bottom-0 z-10 flex cursor-pointer items-center justify-center rounded-full bg-white/90 p-1.5 text-red-500 shadow-sm hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                  aria-label="프로필 이미지 제거"
                >
                  <Icon icon={IconTrash} size={18} />
                </button>
              )}
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept={PROFILE_IMAGE_ACCEPT}
              className="hidden"
              onChange={handleImageChange}
              aria-label="프로필 이미지 파일 선택"
            />
          </div>
          {(profileFileError || imageActions.errorMessage) && (
            <div className="sm:col-span-2 sm:row-start-2">
              <ValidationMessage
                message={profileFileError ?? imageActions.errorMessage}
              />
            </div>
          )}

          <div className="flex min-w-0 flex-col gap-1 sm:col-start-2 sm:row-start-1">
            <h3 className="typo-subtitle5 text-(--color-text-primary)">이름</h3>
            <form
              onSubmit={handleNameSubmit}
              className="flex w-full min-w-0 flex-col gap-3 sm:flex-row sm:items-center"
            >
              <div className="min-w-0 flex-1">
                <label className="sr-only" htmlFor="mypage-name">
                  이름
                </label>
                <input
                  id="mypage-name"
                  value={name}
                  maxLength={MEMBER_NAME_MAX_LENGTH}
                  onChange={(event) => setName(event.target.value)}
                  className={inputClassName}
                />
              </div>
              <button
                type="submit"
                disabled={
                  nameMutation.isPending ||
                  !name.trim() ||
                  name.trim() === info.name
                }
                className="typo-button-md h-11 w-fit shrink-0 self-end cursor-pointer whitespace-nowrap rounded-xl bg-(--color-action-primary) px-5 text-white disabled:cursor-not-allowed disabled:opacity-60 sm:self-auto"
              >
                저장
              </button>
            </form>
            {(nameMutation.errorMessage || nameMutation.successMessage) && (
              <div>
                <ValidationMessage message={nameMutation.errorMessage} />
                <ValidationMessage
                  message={nameMutation.successMessage}
                  tone="neutral"
                />
              </div>
            )}
          </div>
        </div>

        <div className="flex flex-col gap-3 pt-1 sm:flex-row sm:items-center sm:justify-between">
          <div className="min-w-0">
            <h3 className="typo-subtitle5 text-(--color-text-primary)">
              프로필 공개
            </h3>
            <p className="typo-body6 text-(--color-text-tertiary)">
              비공개로 바꾸면 다른 사람에게 참여 프로젝트와 활동 지표가 보이지
              않습니다.
            </p>
          </div>
          <button
            type="button"
            role="switch"
            aria-checked={isProfilePublic}
            aria-label="프로필 공개 여부"
            disabled={profileVisibilityMutation.isPending}
            onClick={handleProfileVisibilityToggle}
            className={`flex h-11 w-fit shrink-0 cursor-pointer items-center gap-2 rounded-full px-3 transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
              isProfilePublic
                ? "bg-(--color-action-primary) text-white"
                : "bg-gray-200 text-(--color-text-secondary)"
            }`}
          >
            <span className="typo-button-md min-w-10 text-center">
              {isProfilePublic ? "공개" : "비공개"}
            </span>
            <span
              aria-hidden="true"
              className={`relative h-6 w-11 rounded-full transition-colors ${
                isProfilePublic ? "bg-white/30" : "bg-gray-300"
              }`}
            >
              <span
                className={`absolute top-0.5 left-0.5 h-5 w-5 rounded-full bg-white shadow-sm transition-transform ${
                  isProfilePublic ? "translate-x-5" : "translate-x-0"
                }`}
              />
            </span>
          </button>
        </div>
        <ValidationMessage message={profileVisibilityMutation.errorMessage} />
      </GlassCard>
    </section>
  );
};
