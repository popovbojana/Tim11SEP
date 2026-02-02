import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Reservation } from '../../../shared/models/reservation';
import { ReservationsApi } from '../../../core/services/reservations-api/reservations-api';

@Component({
  selector: 'app-payment-error-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './error.html',
  styleUrl: '../payment-result.scss',
})
export class Error {
  private route = inject(ActivatedRoute);
  private reservationsApi = inject(ReservationsApi);

  reservationId = signal<number | null>(null);
  reservation = signal<Reservation | null>(null);
  error = signal<string | null>(null);
  loading = signal<boolean>(true);

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const idStr = params.get('reservationId');
      const id = idStr ? Number(idStr) : null;

      if (!id || Number.isNaN(id)) {
        this.loading.set(false);
        this.error.set('Missing reservationId.');
        return;
      }

      this.reservationId.set(id);
      this.load(id);
    });
  }

  private load(id: number) {
    this.loading.set(true);
    this.error.set(null);
    this.reservation.set(null);

    this.reservationsApi.getById(id).subscribe({
      next: (res) => {
        this.reservation.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load reservation.');
        this.loading.set(false);
      },
    });
  }
}
