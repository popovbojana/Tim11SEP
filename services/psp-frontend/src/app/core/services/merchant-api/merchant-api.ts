import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { MerchantCreateRequest, MerchantResponse, PaymentMethod, UpdateMethodsPayload } from '../../../shared/models/merchant';

@Injectable({ providedIn: 'root' })
export class MerchantApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  create(payload: MerchantCreateRequest): Observable<MerchantResponse> {
    return this.http.post<MerchantResponse>(`${this.baseUrl}/api/merchants`, payload);
  }

  get(merchantKey: string): Observable<MerchantResponse> {
    return this.http.get<MerchantResponse>(`${this.baseUrl}/api/merchants/${merchantKey}`);
  }

  getAll(): Observable<MerchantResponse[]> {
    return this.http.get<MerchantResponse[]>(`${this.baseUrl}/api/merchants`);
  }

  getMethods(merchantKey: string): Observable<PaymentMethod[]> {
    return this.http.get<PaymentMethod[]>(
      `${this.baseUrl}/api/merchants/${merchantKey}/methods`
    );
  }

  updateMethods(merchantKey: string, payload: UpdateMethodsPayload): Observable<PaymentMethod[]> {
    return this.http.put<PaymentMethod[]>(
      `${this.baseUrl}/api/merchants/${merchantKey}/methods`,
      payload
    );
  }
}
