import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReservationsApi } from '../../../core/services/reservations-api/reservations-api';
import { Reservation } from '../../../shared/models/reservation';

@Component({
  selector: 'app-my-reservations',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-reservations.html',
  styleUrl: './my-reservations.scss',
})
export class MyReservations implements OnInit {
  activeReservations = signal<Reservation[] | null>(null);
  historyReservations = signal<Reservation[] | null>(null);
  errorMessage = signal<string | null>(null);

  selectedTab = signal<'ACTIVE' | 'HISTORY'>('ACTIVE');

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

  constructor(private reservationsApi: ReservationsApi) {}

  ngOnInit(): void {
    this.loadActive();
    this.loadHistory();
  }

  loadActive(): void {
    this.reservationsApi.getActiveReservations().subscribe({
      next: (data) => this.activeReservations.set(data ?? []),
      error: () => this.errorMessage.set('Failed to load active reservations.'),
    });
  }

  loadHistory(): void {
    this.reservationsApi.getHistory().subscribe({
      next: (data) => this.historyReservations.set(data ?? []),
      error: () => this.errorMessage.set('Failed to load reservation history.'),
    });
  }

  selectTab(tab: 'ACTIVE' | 'HISTORY'): void {
    this.selectedTab.set(tab);
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
}
