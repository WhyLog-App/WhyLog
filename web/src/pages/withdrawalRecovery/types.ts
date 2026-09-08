export interface WithdrawalRecoveryLocationState {
  memberId?: number;
  email?: string;
  challenge?: string | null;
  purgeAt?: string | null;
}
