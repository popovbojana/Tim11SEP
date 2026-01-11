import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Reservation } from '../../../shared/models/reservation';
import { CreateReservationRequest } from '../../../shared/models/create-reservation-request';

@Injectable({ providedIn: 'root' })
export class ReservationsApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  createReservation(
    request: CreateReservationRequest
  ): Observable<Reservation> {
    return this.http.post<Reservation>(
      `${this.baseUrl}/api/reservations`,
      request
    );
  }

  getActiveReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(
      `${this.baseUrl}/api/reservations/active`
    );
  }

  getHistory(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(
      `${this.baseUrl}/api/reservations/history`
    );
  }

  getById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(
      `${this.baseUrl}/api/reservations/${id}`
    )
  }
}
