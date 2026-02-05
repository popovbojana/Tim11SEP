import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { startWith } from 'rxjs';
import { MerchantApi } from '../../../../core/services/merchant-api/merchant-api';
import { PaymentMethodApi } from '../../../../core/services/payment-method-api/payment-method-api';
import { MerchantResponse, MerchantUpdateRequest } from '../../../../shared/models/merchant';

@Component({
  selector: 'app-merchant-details',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './merchant-details.html',
  styleUrl: './merchant-details.scss',
})
export class MerchantDetails implements OnInit {
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private merchantApi = inject(MerchantApi);
  private paymentMethodApi = inject(PaymentMethodApi);

  merchantKey = '';
  merchant = signal<MerchantResponse | null>(null);
  allMethods = signal<string[]>([]);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  saving = signal(false);

  private httpsPattern = /^https:\/\/.+/;
  private bankAccountPattern = /^[0-9-]{13,22}$/;

  form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    successUrl: ['', [Validators.required, Validators.pattern(this.httpsPattern)]],
    failedUrl: ['', [Validators.required, Validators.pattern(this.httpsPattern)]],
    errorUrl: ['', [Validators.required, Validators.pattern(this.httpsPattern)]],
    serviceName: ['', [Validators.required, Validators.minLength(3)]],
    bankAccount: ['', [Validators.required, Validators.pattern(this.bankAccountPattern)]],
    methods: [[] as string[], [Validators.required, Validators.minLength(1)]],
  });

  formValue = toSignal(this.form.valueChanges.pipe(startWith(this.form.getRawValue())));

  isUnchanged = computed(() => {
    const initial = this.merchant();
    const current = this.formValue();
    
    if (!initial || !current || !current.methods) return true;

    const currentMethods = current.methods;
    const initialMethods = initial.activeMethods;

    const methodsMatch = initialMethods.length === currentMethods.length &&
      [...initialMethods].sort().every((v, i) => v === [...currentMethods].sort()[i]);

    return current.fullName === initial.fullName &&
           current.email === initial.email &&
           current.successUrl === initial.successUrl &&
           current.failedUrl === initial.failedUrl &&
           current.errorUrl === initial.errorUrl &&
           current.serviceName === initial.serviceName &&
           current.bankAccount === initial.bankAccount &&
           methodsMatch;
  });

  ngOnInit(): void {
    const key = this.route.snapshot.paramMap.get('merchantKey');
    if (!key) {
      this.errorMessage.set('Missing merchant key in route.');
      return;
    }
    this.merchantKey = key;
    this.loadAllAvailableMethods();
  }

  loadAllAvailableMethods(): void {
    this.paymentMethodApi.getAll().subscribe({
      next: (methods) => {
        this.allMethods.set(methods.map(m => m.name));
        this.load();
      },
      error: () => this.errorMessage.set('Failed to load system payment methods.')
    });
  }

  load(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.merchant.set(null);

    this.merchantApi.get(this.merchantKey).subscribe({
      next: (res) => {
        this.merchant.set(res);
        this.form.patchValue({
          fullName: res.fullName,
          email: res.email,
          successUrl: res.successUrl,
          failedUrl: res.failedUrl,
          errorUrl: res.errorUrl,
          serviceName: res.serviceName,
          bankAccount: res.bankAccount,
          methods: [...res.activeMethods]
        });
        this.form.markAsPristine();
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Failed to load merchant.');
      },
    });
  }

  toggleMethod(method: string): void {
    this.successMessage.set(null);
    const current = this.form.controls.methods.value;
    const next = current.includes(method) 
      ? current.filter(m => m !== method) 
      : [...current, method];

    this.form.controls.methods.setValue(next);
    this.form.controls.methods.markAsTouched();
    this.form.markAsDirty();
  }

  save(): void {
    if (this.form.invalid || this.isUnchanged()) return;

    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.saving.set(true);

    const rawValues = this.form.getRawValue();
    const payload: MerchantUpdateRequest = {
      ...rawValues,
      serviceName: rawValues.serviceName.toUpperCase().trim()
    };

    this.merchantApi.update(this.merchantKey, payload).subscribe({
      next: (updated) => {
        this.saving.set(false);
        this.successMessage.set('Merchant updated successfully.');
        this.merchant.set(updated);
        this.form.markAsPristine();
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Failed to update merchant.');
      },
    });
  }
}