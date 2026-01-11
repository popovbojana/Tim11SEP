import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RentalOffer } from '../../../shared/models/rental-offer';

@Injectable({
  providedIn: 'root',
})
export class OffersApi {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getOffers(): Observable<RentalOffer[]> {
    return this.http.get<RentalOffer[]>(`${this.baseUrl}/api/offers`);
  }

  getOfferById(offerId: number): Observable<RentalOffer> {
    return this.http.get<RentalOffer>(
      `${this.baseUrl}/api/offers/${offerId}`
    );
  }
}
