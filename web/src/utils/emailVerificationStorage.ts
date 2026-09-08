const EMAIL_VERIFICATION_EMAIL_STORAGE_KEY = "whylog:email-verification-email";

export const saveEmailVerificationEmail = (email: string) => {
  const trimmedEmail = email.trim();
  if (!trimmedEmail) return;
  try {
    sessionStorage.setItem(EMAIL_VERIFICATION_EMAIL_STORAGE_KEY, trimmedEmail);
  } catch (error) {
    console.error("이메일 인증 컨텍스트 저장 실패:", error);
  }
};

export const getEmailVerificationEmail = () => {
  try {
    return (
      sessionStorage.getItem(EMAIL_VERIFICATION_EMAIL_STORAGE_KEY)?.trim() ?? ""
    );
  } catch (error) {
    console.error("이메일 인증 컨텍스트 조회 실패:", error);
    return "";
  }
};

export const clearEmailVerificationEmail = () => {
  try {
    sessionStorage.removeItem(EMAIL_VERIFICATION_EMAIL_STORAGE_KEY);
  } catch (error) {
    console.error("이메일 인증 컨텍스트 삭제 실패:", error);
  }
};
