import { Component, OnInit, signal } from '@angular/core';

import { ReservationsApi } from '../../../core/services/reservations-api/reservations-api';
import { Reservation } from '../../../shared/models/reservation';

@Component({
  selector: 'app-my-reservations',
  standalone: true,
  templateUrl: './my-reservations.html',
  styleUrl: './my-reservations.scss',
})
export class MyReservations implements OnInit {
  activeReservations = signal<Reservation[] | null>(null);
  historyReservations = signal<Reservation[] | null>(null);
  errorMessage = signal<string | null>(null);

  selectedTab = signal<'ACTIVE' | 'HISTORY'>('ACTIVE');

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
}
