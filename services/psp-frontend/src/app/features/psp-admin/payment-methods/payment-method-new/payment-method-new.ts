import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PaymentMethodApi } from '../../../../core/services/payment-method-api/payment-method-api';
import { PaymentMethodRequest } from '../../../../shared/models/merchant';

@Component({
  selector: 'app-payment-method-new',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './payment-method-new.html',
  styleUrl: './payment-method-new.scss',
})
export class PaymentMethodNew {
  private fb = inject(FormBuilder);
  private paymentMethodApi = inject(PaymentMethodApi);
  private router = inject(Router);

  errorMessage = signal<string | null>(null);
  loading = signal(false);

  form = this.fb.nonNullable.group({
    name: ['', [
      Validators.required, 
      Validators.minLength(2),
      Validators.pattern(/^[a-zA-Z0-9_-]+$/)
    ]],
    serviceName: ['', [
      Validators.required,
      Validators.pattern(/^[a-zA-Z0-9_-]+$/)
    ]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.loading.set(true);

    const payload: PaymentMethodRequest = this.form.getRawValue();
    payload.name = payload.name.toUpperCase().trim();
    payload.serviceName = payload.serviceName.toUpperCase().trim();

    this.paymentMethodApi.add(payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/payment-methods']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Failed to register payment method.');
      },
    });
  }
}