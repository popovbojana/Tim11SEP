import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

export interface RedirectResponse {
  redirectUrl: string;
}

export interface ExecutePaymentRequest {
  pan: string;
  securityCode: string;
  cardHolderName: string;
  expiry: string;
}

@Injectable({ providedIn: 'root' })
export class BankPaymentApi {
  private readonly baseUrl = environment.apiBaseUrl;
  private http = inject(HttpClient);

  execute(bankPaymentId: number, payload: ExecutePaymentRequest): Observable<RedirectResponse> {
    return this.http.post<RedirectResponse>(
      `${this.baseUrl}/api/payments/${bankPaymentId}/execute`,
      payload
    );
  }
}
