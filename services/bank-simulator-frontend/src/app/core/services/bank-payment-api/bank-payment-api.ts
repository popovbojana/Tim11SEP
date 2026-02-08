import { Injectable } from '@angular/core';
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
  reason?: 'INSUFFICIENT_FUNDS' | 'INVALID_CARD_DATA' | string;
  redirectUrl?: string;
}
export interface AuthorizeResponse {
  status: 'SUCCESS' | 'FAILED';
  globalTransactionId?: string;
  acquirerTimestamp?: string;
  reason?: 'INSUFFICIENT_FUNDS' | 'INVALID_CARD_DATA' | string;
  redirectUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class BankPaymentApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  execute(
    bankPaymentId: number,
    payload: {
      pan: string;
      securityCode: string;
      cardHolderName: string;
      expiry: string;
    }
  ): Observable<AuthorizeResponse> {
    return this.http.post<AuthorizeResponse>(
      `${this.baseUrl}/api/payments/${bankPaymentId}/execute`,
      payload
    );
  }
}
