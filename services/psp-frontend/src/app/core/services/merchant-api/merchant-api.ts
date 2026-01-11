import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';


export type PaymentMethod = 'CARD' | 'QR' | 'PAYPAL' | 'CRYPTO';

export type UpdateMethodsPayload = {
  methods: PaymentMethod[];
};

@Injectable({ providedIn: 'root' })
export class MerchantApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getMethods(merchantKey: string): Observable<PaymentMethod[]> {
    return this.http.get<PaymentMethod[]>(
      `${this.baseUrl}/api/merchants/${merchantKey}/methods`
    );
  }

  updateMethods(merchantKey: string, payload: { methods: PaymentMethod[] }) {
  return this.http.put<void>(`${this.baseUrl}/api/merchants/${merchantKey}/methods`, payload);
}

}
