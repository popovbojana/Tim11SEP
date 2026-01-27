export type PaymentMethod =
  | 'CARD'
  | 'QR'
  | 'PAYPAL'
  | 'CRYPTO';

export interface MerchantResponse {
  id: number;
  merchantKey: string;
  activeMethods: PaymentMethod[];
}

export type MerchantCreateRequest = {
  merchantKey: string;
};

export type UpdateMethodsPayload = {
  methods: PaymentMethod[];
};
