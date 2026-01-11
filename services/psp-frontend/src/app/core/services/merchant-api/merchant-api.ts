import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class MerchantApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getMethods(merchantKey: string): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.baseUrl}/api/merchants/${merchantKey}/methods`
    );
  }
}
