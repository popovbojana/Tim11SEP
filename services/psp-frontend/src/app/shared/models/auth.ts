export type LoginRequest = {
  email: string;
  password: string;
};

export type MfaVerificationRequest = {
  email: string;
  code: string;
};

export type AuthResponse = {
  token: string;
  mfaRequired: boolean;
  email: string;
};