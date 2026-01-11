import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RedirectResponse } from '../bank-payment-api/bank-payment-api';

export type QrImageResponse = {
  qrImageBase64: string;
  qrText: string;
};

@Injectable({ providedIn: 'root' })
export class QrPaymentApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getQr(bankPaymentId: number): Observable<QrImageResponse> {
    return this.http.get<QrImageResponse>(`${this.baseUrl}/api/payments/${bankPaymentId}/qr`);
  }

  confirm(bankPaymentId: number, qrText: string): Observable<RedirectResponse> {
    return this.http.post<RedirectResponse>(
      `${this.baseUrl}/api/payments/${bankPaymentId}/qr/confirm`,
      { qrText }
    );
  }
}
