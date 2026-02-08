import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import {
  BankPaymentApi,
  ExecutePaymentRequest,
} from '../../../core/services/bank-payment-api/bank-payment-api';

type CardBrand = 'VISA' | 'MASTERCARD' | null;

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout {
  error: string | null = null;
  loading = false;
  bankPaymentId: number | null = null;
  detectedBrand: CardBrand = null;
  form;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private bankPaymentApi: BankPaymentApi,
  ) {
    this.form = this.fb.nonNullable.group({
      cardHolderName: ['', [Validators.required]],
      pan: ['', [Validators.required]],
      expiry: ['', [Validators.required]],
      securityCode: ['', [Validators.required]],
    });

    this.form.controls.pan.valueChanges.subscribe((value) => {
      const formatted = this.formatPan(value ?? '');
      if (formatted !== value) {
        this.form.controls.pan.patchValue(formatted, { emitEvent: false });
      }
      const digits = this.onlyDigits(formatted);
      this.detectedBrand = this.detectBrand(digits);
    });

    this.form.controls.expiry.valueChanges.subscribe((value) => {
      const formatted = this.formatExpiry(value ?? '');
      if (formatted !== value) {
        this.form.controls.expiry.patchValue(formatted, { emitEvent: false });
      }
    });

    this.route.paramMap.subscribe((params) => {
      const idStr = params.get('bankPaymentId');
      const id = idStr ? Number(idStr) : null;
      if (!id || Number.isNaN(id)) {
        this.error = 'Invalid bank payment id';
        return;
      }
      this.bankPaymentId = id;
    });
  }

  submit(): void {
    this.error = null;
    if (this.loading) return;
    if (!this.bankPaymentId) {
      this.error = 'Missing bank payment id';
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    const rawValues = this.form.getRawValue();
    const payload: ExecutePaymentRequest = {
      ...rawValues,
      pan: this.onlyDigits(rawValues.pan)
    };

    this.bankPaymentApi.execute(this.bankPaymentId, payload).subscribe({
      next: (res: any) => {
        this.loading = false;
        if (res.status === 'SUCCESS') {
          alert('✅ Payment successful!');
          window.location.href = 'http://localhost:4200/reservations';
        } else {
          if (res.reason === 'INSUFFICIENT_FUNDS') {
            this.error = 'Denied: insufficient funds.';
          } else if (res.reason === 'INVALID_CARD_DATA') {
            this.error = 'Denied: invalid card data.';
          } else if (res.reason === 'INVALID_PAN') {
            this.error = 'Denied: invalid card number.';
          } else if (res.reason === 'INVALID_CVV') {
            this.error = 'Denied: invalid CVV';
          } else if (res.reason === 'EXPIRED_CARD') {
            this.error = 'Denied: card expired.';
          } else {
            this.error = 'Payment denied. Try again.';
          }
          this.form.controls.securityCode.reset();
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Bank communication error';
      },
    });
  }

  private onlyDigits(input: string): string {
    return (input ?? '').replace(/\D/g, '');
  }

  private formatPan(input: string): string {
    const digits = this.onlyDigits(input).slice(0, 19);
    return digits.replace(/(.{4})/g, '$1 ').trim();
  }

  private formatExpiry(input: string): string {
    const digits = this.onlyDigits(input).slice(0, 4);
    if (digits.length <= 2) return digits;
    return `${digits.slice(0, 2)}/${digits.slice(2)}`;
  }

  private detectBrand(panDigits: string): CardBrand {
    if (!panDigits) return null;
    if (panDigits.startsWith('4')) return 'VISA';
    if (panDigits.length >= 2) {
      const p2 = Number(panDigits.slice(0, 2));
      if (p2 >= 51 && p2 <= 55) return 'MASTERCARD';
    }
    if (panDigits.length >= 4) {
      const p4 = Number(panDigits.slice(0, 4));
      if (p4 >= 2221 && p4 <= 2720) return 'MASTERCARD';
    }
    return null;
  }

  isBrandActive(brand: Exclude<CardBrand, null>): boolean {
    if (!this.detectedBrand) return true;
    return this.detectedBrand === brand;
  }
}