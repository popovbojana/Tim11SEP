import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { MerchantCreateRequest, MerchantResponse, UpdateMethodsPayload } from '../../../shared/models/merchant';

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

  getMethods(merchantKey: string): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.baseUrl}/api/merchants/${merchantKey}/methods`
    );
  }

  updateMethods(merchantKey: string, payload: UpdateMethodsPayload): Observable<string[]> {
    return this.http.put<string[]>(
      `${this.baseUrl}/api/merchants/${merchantKey}/methods`,
      payload
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/merchants/${id}`);
  }
}