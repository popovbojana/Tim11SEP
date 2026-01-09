import { Component, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';

import { OffersApi } from '../../../core/services/offers-api/offers-api';
import { RentalOffer } from '../../../shared/models/rental-offer';

@Component({
  selector: 'app-offers-list',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './offers-list.html',
  styleUrl: './offers-list.scss',
})
export class OffersList implements OnInit {
  offers = signal<RentalOffer[] | null>(null);
  errorMessage = signal<string | null>(null);

  constructor(private offersApi: OffersApi) {}

  ngOnInit(): void {
    this.loadOffers();
  }

  loadOffers(): void {
    this.errorMessage.set(null);
    this.offers.set(null);

    this.offersApi.getOffers().subscribe({
      next: (data) => {
        this.offers.set(data ?? []);
      },
      error: () => {
        this.errorMessage.set('Could not load offers. Please try again.');
        this.offers.set([]);
      },
    });
  }

  getVehicleLabel(offer: RentalOffer): string {
    const v = offer.vehicle;
    if (!v) return '';
    return `${v.brand} ${v.model} (${v.type})`;
  }
}
