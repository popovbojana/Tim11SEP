import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PaymentMethodApi } from '../../../../core/services/payment-method-api/payment-method-api';
import { PaymentMethodResponse } from '../../../../shared/models/merchant';

@Component({
  selector: 'app-payment-methods-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-methods-list.html',
  styleUrl: './payment-methods-list.scss',
})
export class PaymentMethodsList implements OnInit {
  private paymentMethodApi = inject(PaymentMethodApi);

  methods = signal<PaymentMethodResponse[] | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage.set(null);
    this.methods.set(null);

    this.paymentMethodApi.getAll().subscribe({
      next: (res) => {
        this.methods.set(res);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Failed to load payment methods.');
        this.methods.set([]);
      },
    });
  }

  deleteMethod(id: number): void {
    if (!confirm('Are you sure you want to delete this payment method? It will be removed from all merchants.')) {
      return;
    }

    this.paymentMethodApi.delete(id).subscribe({
      next: () => {
        this.load();
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Failed to delete payment method.');
      }
    });
  }

  trackById(_: number, item: PaymentMethodResponse): number {
    return item.id;
  }
}