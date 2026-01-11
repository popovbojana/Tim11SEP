import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PaymentsApi } from '../../../core/services/payment-api/payment-api';
import { Payment } from '../../../shared/models/payment';

@Component({
  selector: 'app-checkout',
  standalone: true,
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout {
  private route = inject(ActivatedRoute);
  private pspPaymentsApi = inject(PaymentsApi);

  paymentId = signal<number | null>(null);
  payment = signal<Payment | null>(null);
  error = signal<string | null>(null);

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const idStr = params.get('paymentId');
      const id = idStr ? Number(idStr) : null;

      if (!id || Number.isNaN(id)) {
        this.error.set('Invalid payment id');
        return;
      }

      this.paymentId.set(id);
      this.loadPayment(id);
    });
  }

  private loadPayment(id: number) {
    this.error.set(null);
    this.payment.set(null);

    this.pspPaymentsApi.getPayment(id).subscribe({
      next: (res) => this.payment.set(res),
      error: () => this.error.set('Failed to load payment.'),
    });
  }

  payByCard(): void {
    const id = this.paymentId();
    if (!id) return;

    this.error.set(null);

    this.pspPaymentsApi.startCardPayment(id).subscribe({
      next: (res) => {
        console.log(res.redirectUrl)
        window.location.href = res.redirectUrl;
      },
      error: () => this.error.set('Failed to start card payment.'),
    });
  }
}
