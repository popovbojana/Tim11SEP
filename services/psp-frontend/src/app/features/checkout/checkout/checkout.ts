import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PaymentsApi } from '../../../core/services/payment-api/payment-api';
import { Payment } from '../../../shared/models/payment';
import { MerchantApi } from '../../../core/services/merchant-api/merchant-api';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout {
  private route = inject(ActivatedRoute);
  private pspPaymentsApi = inject(PaymentsApi);
  private merchantApi = inject(MerchantApi);

  paymentId = signal<number | null>(null);
  payment = signal<Payment | null>(null);
  methods = signal<string[] | null>(null);
  error = signal<string | null>(null);
  processingMethod = signal<string | null>(null);

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const idStr = params.get('paymentId');
      const id = idStr ? Number(idStr) : null;

      if (!id || Number.isNaN(id)) {
        this.error.set('Invalid payment.');
        return;
      }

      this.paymentId.set(id);
      this.loadPayment(id);
    });
  }

  private loadPayment(id: number) {
    this.error.set(null);
    this.pspPaymentsApi.getPayment(id).subscribe({
      next: (res) => {
        this.payment.set(res);
        if (res.merchantKey) {
          this.loadMerchantMethods(res.merchantKey);
        } else {
          this.error.set('Merchant data missing.');
        }
      },
      error: () => this.error.set('Failed to load payment details.'),
    });
  }

  private loadMerchantMethods(merchantKey: string) {
    this.merchantApi.getMethods(merchantKey).subscribe({
      next: (m) => this.methods.set((m ?? []).map(x => x.toUpperCase())),
      error: () => this.error.set('Failed to load available payment methods.'),
    });
  }

  onPay(method: string): void {
    const id = this.paymentId();
    if (!id || this.processingMethod()) return;

    this.error.set(null);
    this.processingMethod.set(method);

    this.pspPaymentsApi.startPayment(id, method).subscribe({
      next: (res) => (window.location.href = res.redirectUrl),
      error: (err) => {
        this.error.set(`Service for ${method} is currently unavailable.`);
        this.processingMethod.set(null);
      }
    });
  }
}