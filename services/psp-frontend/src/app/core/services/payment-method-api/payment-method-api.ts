import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PaymentMethodRequest, PaymentMethodResponse } from '../../../shared/models/merchant';

@Injectable({
  providedIn: 'root',
})
export class PaymentMethodApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getAll(): Observable<PaymentMethodResponse[]> {
    return this.http.get<PaymentMethodResponse[]>(`${this.baseUrl}/api/payment-methods`);
  }

  add(payload: PaymentMethodRequest): Observable<PaymentMethodResponse> {
    return this.http.post<PaymentMethodResponse>(`${this.baseUrl}/api/payment-methods`, payload);
  }

  update(id: number, payload: PaymentMethodRequest): Observable<PaymentMethodResponse> {
    return this.http.put<PaymentMethodResponse>(`${this.baseUrl}/api/payment-methods/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/payment-methods/${id}`);
  }

  findById(id: number): Observable<PaymentMethodResponse> {
    return this.http.get<PaymentMethodResponse>(`${this.baseUrl}/api/payment-methods/${id}`);
  }
}