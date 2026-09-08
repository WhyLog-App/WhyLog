export interface EmailVerificationLocationState {
  email?: string;
  profileImage?: File | null;
  profileImageExpiresAt?: number | null;
  deliveryErrorMessage?: string;
  profileImagePersistenceWarning?: string | null;
}
