import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Reservation } from '../../../shared/models/reservation';
import { ReservationsApi } from '../../../core/services/reservations-api/reservations-api';

@Component({
  selector: 'app-payment-success-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './success.html',
  styleUrl: '../payment-result.scss',
})
export class Success {
  private route = inject(ActivatedRoute);
  private reservationsApi = inject(ReservationsApi);

  reservationId = signal<number | null>(null);
  reservation = signal<Reservation | null>(null);
  error = signal<string | null>(null);
  loading = signal<boolean>(true);

  private readonly dateOnlyFmt = new Intl.DateTimeFormat('sr-RS', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  private readonly dateTimeFmt = new Intl.DateTimeFormat('sr-RS', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  constructor() {
    this.route.queryParamMap.subscribe((params) => {
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

  formatDateOnly(value: string | null | undefined): string {
    if (!value) return '-';
    const d = new Date(`${value}T00:00:00`);
    if (Number.isNaN(d.getTime())) return value;
    return this.dateOnlyFmt.format(d);
  }

  formatDateTime(value: string | null | undefined): string {
    if (!value) return '-';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return value;
    return this.dateTimeFmt.format(d);
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
