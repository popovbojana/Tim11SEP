import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

export interface RedirectResponse {
  redirectUrl: string;
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
  ): Observable<RedirectResponse> {
    return this.http.post<RedirectResponse>(
      `${this.baseUrl}/api/payments/${bankPaymentId}/execute`,
      payload
    );
  }

}
