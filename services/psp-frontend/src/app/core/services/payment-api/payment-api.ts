import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Payment } from '../../../shared/models/payment';
import { Observable } from 'rxjs';

export interface StartPaymentResponse {
  redirectUrl: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentsApi {
  private readonly baseUrl = environment.apiBaseUrl;
  private http = inject(HttpClient);

  getPayment(paymentId: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.baseUrl}/api/payments/${paymentId}`);
  }

  startPayment(paymentId: number, methodName: string): Observable<StartPaymentResponse> {
    const params = new HttpParams().set('methodName', methodName);
    
    return this.http.post<StartPaymentResponse>(
      `${this.baseUrl}/api/payments/${paymentId}/start`,
      {}, 
      { params }
    );
  }
}