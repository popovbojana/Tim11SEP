export type PaymentStatus = 'CREATED' | 'IN_PROGRESS' | 'SUCCESS' | 'FAILED' | 'ERROR';

export type Payment = {
  id: number;
  amount: number;
  currency: string;
  status: PaymentStatus;
  merchantKey?: string;
  merchantOrderId?: string;
  successUrl: string;
  failedUrl: string;
  errorUrl: string;
};
