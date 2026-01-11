import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

type QrImageResponse = {
  qrImageBase64: string;
};

@Injectable({ providedIn: 'root' })
export class QrPaymentApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getQr(bankPaymentId: number): Observable<QrImageResponse> {
    return this.http.get<QrImageResponse>(`${this.baseUrl}/api/payments/${bankPaymentId}/qr`);
  }
}
