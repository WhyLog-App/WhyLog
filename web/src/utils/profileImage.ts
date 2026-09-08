export const PROFILE_IMAGE_ACCEPT = "image/jpeg,image/png,image/webp";

const MAX_PROFILE_IMAGE_BYTES = 5 * 1024 * 1024;
const ALLOWED_PROFILE_IMAGE_TYPES = new Set(PROFILE_IMAGE_ACCEPT.split(","));

export const validateProfileImageFile = (file: File) => {
  if (!ALLOWED_PROFILE_IMAGE_TYPES.has(file.type)) {
    return "JPEG, PNG, WebP 이미지만 업로드할 수 있습니다.";
  }
  if (file.size > MAX_PROFILE_IMAGE_BYTES) {
    return "프로필 이미지는 5MB 이하만 업로드할 수 있습니다.";
  }
  return null;
};
