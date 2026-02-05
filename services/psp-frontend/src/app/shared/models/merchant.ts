export interface PaymentMethodResponse {
  id: number;
  name: string;
  serviceName: string;
}

export interface MerchantResponse {
  id: number;
  merchantKey: string;
  fullName: string;
  email: string;
  successUrl: string;
  failedUrl: string;
  errorUrl: string;
  serviceName: string;
  bankAccount: string;
  activeMethods: string[];
}

export interface MerchantCreateRequest {
  merchantKey: string;
  merchantPassword: string;
  fullName: string;
  email: string;
  successUrl: string;
  failedUrl: string;
  errorUrl: string;
  serviceName: string;
  bankAccount: string;
  methods: string[];
}

export interface MerchantUpdateRequest {
  fullName: string;
  email: string;
  successUrl: string;
  failedUrl: string;
  errorUrl: string;
  serviceName: string;
  bankAccount: string;
  methods: string[];
}

export interface PaymentMethodRequest {
  name: string;
  serviceName: string;
}