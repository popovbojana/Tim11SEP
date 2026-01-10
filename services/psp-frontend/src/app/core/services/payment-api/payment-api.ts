import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Payment } from '../../../shared/models/payment';

export interface StartPaymentResponse {
  redirectUrl: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentsApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getPayment(paymentId: number) {
    return this.http.get<Payment>(`${this.baseUrl}/api/payments/${paymentId}`);
  }

  startCardPayment(paymentId: number) {
    return this.http.post<StartPaymentResponse>(
      `${this.baseUrl}/api/payments/${paymentId}/start/card`,
      null
    );
  }
}
