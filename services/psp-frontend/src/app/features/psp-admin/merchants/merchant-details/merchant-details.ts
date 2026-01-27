import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MerchantApi } from '../../../../core/services/merchant-api/merchant-api';
import {
  MerchantResponse,
  PaymentMethod,
  UpdateMethodsPayload,
} from '../../../../shared/models/merchant';

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

  merchantKey = '';

  merchant = signal<MerchantResponse | null>(null);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  saving = false;

  readonly allMethods: PaymentMethod[] = ['CARD', 'QR', 'PAYPAL', 'CRYPTO'];

  form = this.fb.nonNullable.group({
    methods: this.fb.nonNullable.control<PaymentMethod[]>([], {
      validators: [Validators.required],
    }),
  });

  ngOnInit(): void {
    const key = this.route.snapshot.paramMap.get('merchantKey');
    if (!key) {
      this.errorMessage.set('Missing merchant key in route.');
      this.merchant.set({ id: 0, merchantKey: '-', activeMethods: [] });
      return;
    }

    this.merchantKey = key;
    this.load();
  }

  load(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.merchant.set(null);

    this.merchantApi.get(this.merchantKey).subscribe({
      next: (res) => {
        this.merchant.set(res);

        const current = (res.activeMethods ?? []) as PaymentMethod[];
        this.form.controls.methods.setValue(current);
        this.form.markAsPristine();
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Failed to load merchant.');
        this.merchant.set({ id: 0, merchantKey: this.merchantKey, activeMethods: [] });
      },
    });
  }

  isSelected(method: PaymentMethod): boolean {
    return this.form.controls.methods.value.includes(method);
  }

  toggleMethod(method: PaymentMethod): void {
    this.successMessage.set(null);

    const current = this.form.controls.methods.value;

    if (current.includes(method)) {
      this.form.controls.methods.setValue(current.filter((m) => m !== method));
    } else {
      this.form.controls.methods.setValue([...current, method]);
    }

    this.form.controls.methods.markAsTouched();
    this.form.markAsDirty();
  }

  save(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const selected = this.form.controls.methods.value;

    if (!selected || selected.length === 0) {
      this.form.controls.methods.markAsTouched();
      this.errorMessage.set('Select at least one payment method.');
      return;
    }

    this.saving = true;

    const payload: UpdateMethodsPayload = { methods: selected };

    this.merchantApi.updateMethods(this.merchantKey, payload).subscribe({
      next: (updated) => {
        this.saving = false;
        this.successMessage.set('Payment methods saved.');

        const nextMethods = (updated ?? []) as PaymentMethod[];
        this.form.controls.methods.setValue(nextMethods);
        this.form.markAsPristine();

        const currentMerchant = this.merchant();
        if (currentMerchant) {
          this.merchant.set({ ...currentMerchant, activeMethods: nextMethods as any });
        }
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage.set(err?.error?.message ?? 'Failed to save payment methods.');
      },
    });
  }
}
