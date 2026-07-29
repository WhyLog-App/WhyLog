import { useMutation } from "@tanstack/react-query";
import { uploadMemberProfileImage } from "@/apis/auth";

export const useUploadMemberProfileImage = () => {
  return useMutation({
    mutationFn: uploadMemberProfileImage,
  });
};
