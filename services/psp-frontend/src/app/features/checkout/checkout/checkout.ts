import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PaymentsApi } from '../../../core/services/payment-api/payment-api';
import { Payment } from '../../../shared/models/payment';
import { MerchantApi } from '../../../core/services/merchant-api/merchant-api';

@Component({
  selector: 'app-checkout',
  standalone: true,
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
    this.payment.set(null);
    this.methods.set(null);

    this.pspPaymentsApi.getPayment(id).subscribe({
      next: (res) => {
        this.payment.set(res);

        const merchantKey = res.merchantKey;
        if (!merchantKey) {
          this.error.set('Missing merchant information for this payment.');
          return;
        }

        this.loadMerchantMethods(merchantKey);
      },
      error: () => this.error.set('Failed to load payment.'),
    });
  }

private loadMerchantMethods(merchantKey: string) {
    this.merchantApi.getMethods(merchantKey).subscribe({
      next: (m) => {
        this.methods.set((m ?? []).map(x => x.toUpperCase()));
      },
      error: () => this.error.set('Failed to load available payment methods.'),
    });
  }

  hasMethod(method: string): boolean {
    return (this.methods() ?? []).includes(method.toUpperCase());
  }

  payByCard(): void {
    const id = this.paymentId();
    if (!id) return;

    this.error.set(null);

    this.pspPaymentsApi.startCardPayment(id).subscribe({
      next: (res) => (window.location.href = res.redirectUrl),
      error: () => this.error.set('Failed to start card payment.'),
    });
  }

  payByQr(): void {
    const id = this.paymentId();
    if (!id) return;

    this.error.set(null);

    this.pspPaymentsApi.startQrPayment(id).subscribe({
      next: (res) => (
        window.location.href = res.redirectUrl
      ),
      error: () => this.error.set('Failed to start card payment.'),
    });
  }

  payByPaypal(): void {
    alert("paypal coming soon!")
  }

  payByCrypto(): void {
    alert("crypto coming soon!")
  }
}
