export interface PaymentMethodResponse {
  id: number;
  name: string;
  serviceName: string;
}

export interface MerchantResponse {
  id: number;
  merchantKey: string;
  activeMethods: string[];
}

export type MerchantCreateRequest = {
  merchantKey: string;
};

export type UpdateMethodsPayload = {
  methods: string[];
};

export interface PaymentMethodRequest {
  name: string;
  serviceName: string;
}