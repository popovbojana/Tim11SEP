export type PaymentStatus = 'CREATED' | 'IN_PROGRESS' | 'SUCCESS' | 'FAILED';

export type Payment = {
  id: number;
  amount: number;
  status: PaymentStatus;
  merchantKey?: string;
  merchantOrderId?: string;
};
