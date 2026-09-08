import { clearEmailVerificationEmail } from "@/utils/emailVerificationStorage";
import { clearSignupProfileImageDraft } from "@/utils/signupProfileImageDraftStorage";

export const clearAuthFlowState = async () => {
  clearEmailVerificationEmail();
  await clearSignupProfileImageDraft();
};
