import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';

import { OffersApi } from '../../../core/services/offers-api/offers-api';

import { RentalOffer } from '../../../shared/models/rental-offer';
import { CreateReservationRequest } from '../../../shared/models/create-reservation-request';
import { PaymentApi } from '../../../core/services/payment-api/payment-api';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-create-reservation',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule],
  templateUrl: './create-reservation.html',
  styleUrl: './create-reservation.scss',
})
export class CreateReservation implements OnInit {
  offer = signal<RentalOffer | null>(null);
  isLoaded = signal(false);
  errorMessage = signal<string | null>(null);

  selectedServiceIds = signal<Set<number>>(new Set<number>());

  form;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private offersApi: OffersApi,
    private paymentApi: PaymentApi
  ) {
    this.form = this.fb.nonNullable.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.loadOffer();
  }

  private loadOffer(): void {
    this.errorMessage.set(null);
    this.offer.set(null);
    this.isLoaded.set(false);
    this.selectedServiceIds.set(new Set<number>());
    this.form.reset();

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
        this.errorMessage.set('Could not load offer for reservation.');
        this.isLoaded.set(true);
      },
    });
  }

  toggleService(serviceId: number): void {
    const next = new Set(this.selectedServiceIds());
    if (next.has(serviceId)) next.delete(serviceId);
    else next.add(serviceId);
    this.selectedServiceIds.set(next);
  }

  isServiceSelected(serviceId: number): boolean {
    return this.selectedServiceIds().has(serviceId);
  }

  submit(): void {
    this.errorMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const offer = this.offer();
    if (!offer) return;

    const payload: CreateReservationRequest = {
      offerId: offer.id,
      startDate: this.form.getRawValue().startDate,
      endDate: this.form.getRawValue().endDate,
      selectedAdditionalServiceIds: Array.from(this.selectedServiceIds()),
    };

    this.paymentApi.initPayment(payload).subscribe({
      next: (res) => {
        console.log(res.redirectUrl);
        window.location.href = environment.pspBaseUrl + res.redirectUrl;
      },
      error: () => {
        this.errorMessage.set('Reservation failed. Please try again.');
      },
    });
  }
}
