import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MerchantApi } from '../../../../core/services/merchant-api/merchant-api';
import { PaymentMethodApi } from '../../../../core/services/payment-method-api/payment-method-api';
import { MerchantCreateRequest } from '../../../../shared/models/merchant';

@Component({
  selector: 'app-merchant-create',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './merchant-create.html',
  styleUrl: './merchant-create.scss',
})
export class MerchantCreate implements OnInit {
  private fb = inject(FormBuilder);
  private merchantApi = inject(MerchantApi);
  private paymentMethodApi = inject(PaymentMethodApi);
  private router = inject(Router);

  errorMessage = signal<string | null>(null);
  loading = signal(false);
  availableMethods = signal<string[]>([]);
  dropdownOpen = signal(false);

  private httpsPattern = /^https:\/\/.+/;

  form = this.fb.nonNullable.group({
    merchantKey: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[A-Za-z0-9._-]+$/)]],
    merchantPassword: ['', [Validators.required, Validators.minLength(8)]],
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    successUrl: ['', [Validators.required, Validators.pattern(this.httpsPattern)]],
    failedUrl: ['', [Validators.required, Validators.pattern(this.httpsPattern)]],
    errorUrl: ['', [Validators.required, Validators.pattern(this.httpsPattern)]],
    webhookUrl: ['', [Validators.pattern(this.httpsPattern)]],
    methods: [[] as string[], [Validators.required, Validators.minLength(1)]]
  });

  ngOnInit(): void {
    this.paymentMethodApi.getAll().subscribe({
      next: (res) => this.availableMethods.set(res.map(m => m.name)),
      error: () => this.errorMessage.set('Failed to load payment methods.')
    });
  }

  toggleMethod(method: string): void {
    const current = this.form.controls.methods.value;
    const next = current.includes(method) 
      ? current.filter(m => m !== method) 
      : [...current, method];
    
    this.form.controls.methods.setValue(next);
    this.form.controls.methods.markAsDirty();
    this.form.controls.methods.markAsTouched();
  }

  submit(): void {
    if (this.form.invalid) return;

    this.errorMessage.set(null);
    this.loading.set(true);

    const payload: MerchantCreateRequest = this.form.getRawValue();

    this.merchantApi.create(payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/merchants']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Failed to create merchant.');
      },
    });
  }
}