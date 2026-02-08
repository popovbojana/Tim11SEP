import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

export interface RedirectResponse {
  redirectUrl: string;
}

export interface AuthorizeResponse {
  status: 'SUCCESS' | 'FAILED';
  globalTransactionId?: string;
  acquirerTimestamp?: string;
  reason?: 'INSUFFICIENT_FUNDS' | 'INVALID_CARD_DATA' | 'INVALID_PAN' | 'INVALID_CVV' | 'EXPIRED_CARD' | string;
  redirectUrl?: string;
}

export interface ExecutePaymentRequest {
  pan: string;
  securityCode: string;
  cardHolderName: string;
  expiry: string;
}

@Injectable({ providedIn: 'root' })
export class BankPaymentApi {
  private readonly baseUrl = environment.apiBaseUrl;
  private http = inject(HttpClient);

  execute(bankPaymentId: number, payload: ExecutePaymentRequest): Observable<AuthorizeResponse> {
    return this.http.post<AuthorizeResponse>(
      `${this.baseUrl}/api/payments/${bankPaymentId}/execute`,
      payload
    );
  }
}