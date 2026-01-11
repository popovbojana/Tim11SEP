import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BankPaymentApi } from '../../../../core/services/bank-payment-api/bank-payment-api';

@Component({
  selector: 'app-checkout',
  standalone: true,
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout {
  private route = inject(ActivatedRoute);
  private bankPaymentApi = inject(BankPaymentApi);

  bankPaymentId = signal<number | null>(null);
  error = signal<string | null>(null);
  isLoading = signal(false);

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const idStr = params.get('bankPaymentId');
      const id = idStr ? Number(idStr) : null;

      if (!id || Number.isNaN(id)) {
        this.error.set('Invalid bank payment id');
        return;
      }

      this.bankPaymentId.set(id);
    });
  }

  success(): void {
    if (!this.bankPaymentId()) return;

    this.isLoading.set(true);

    this.bankPaymentApi.execute(this.bankPaymentId()!, true).subscribe({
      next: (res) => {
        console.log(res.redirectUrl);
        window.location.href = res.redirectUrl;
      },
      error: () => {
        this.isLoading.set(false);
        alert('Error sending SUCCESS');
      },
    });
  }

  fail(): void {
    if (!this.bankPaymentId()) return;

    this.isLoading.set(true);

    this.bankPaymentApi.execute(this.bankPaymentId()!, false).subscribe({
      next: (res) => {
        window.location.href = res.redirectUrl;
      },
      error: () => {
        this.isLoading.set(false);
        alert('Error sending FAIL');
      },
    });
  }
}
