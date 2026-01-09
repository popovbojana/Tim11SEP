import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';

import { OffersApi } from '../../../core/services/offers-api/offers-api';
import { RentalOffer } from '../../../shared/models/rental-offer';
import { TokenService } from '../../../core/services/token/token';

@Component({
  selector: 'app-offer-details',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './offer-details.html',
  styleUrl: './offer-details.scss',
})
export class OfferDetails implements OnInit {
  offer = signal<RentalOffer | null>(null);
  isLoaded = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private offersApi: OffersApi,
    private tokenService: TokenService
  ) {}

  ngOnInit(): void {
    this.loadOffer();
  }

  isLoggedIn(): boolean {
    return this.tokenService.isLoggedIn();
  }

  loadOffer(): void {
    this.errorMessage.set(null);
    this.offer.set(null);
    this.isLoaded.set(false);

    const idParam = this.route.snapshot.paramMap.get('id');
    const offerId = idParam ? Number(idParam) : NaN;

    if (Number.isNaN(offerId) || offerId <= 0) {
      this.errorMessage.set('Invalid offer id.');
      this.isLoaded.set(true);
      return;
    }

    this.offersApi.getOfferById(offerId).subscribe({
      next: (data) => {
        this.offer.set(data);
        this.isLoaded.set(true);
      },
      error: () => {
        this.errorMessage.set('Could not load offer details. Please try again.');
        this.isLoaded.set(true);
      },
    });
  }

  getVehicleLabel(): string {
    const offer = this.offer();
    if (!offer?.vehicle) return '';
    const v = offer.vehicle;
    return `${v.brand} ${v.model} (${v.type})`;
  }

  getAdditionalServicesTotalPerDay(): number {
    const offer = this.offer();
    if (!offer?.additionalServices?.length) return 0;
    return offer.additionalServices.reduce(
      (sum, s) => sum + (s.pricePerDay ?? 0),
      0
    );
  }

  getTotalBasePerDay(): number {
    const offer = this.offer();
    if (!offer) return 0;
    const vehicle = offer.vehicle?.pricePerDay ?? 0;
    const insurance = offer.insurancePackage?.pricePerDay ?? 0;
    const base = offer.basePricePerDay ?? 0;
    return base + vehicle + insurance;
  }
}
