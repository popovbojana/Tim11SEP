import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CreateReservationRequest } from '../../../shared/models/create-reservation-request';

export type PaymentResponse = {
  redirectUrl: string;
};

@Injectable({ providedIn: 'root' })
export class PaymentApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  initPayment(request: CreateReservationRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.baseUrl}/api/payments/init`,
      request
    );
  }
}