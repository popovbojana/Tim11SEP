import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MerchantApi } from '../../../../core/services/merchant-api/merchant-api';
import { PaymentMethodApi } from '../../../../core/services/payment-method-api/payment-method-api';
import {
  MerchantResponse,
  UpdateMethodsPayload
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
  private paymentMethodApi = inject(PaymentMethodApi);

  merchantKey = '';
  merchant = signal<MerchantResponse | null>(null);
  allMethods = signal<string[]>([]);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  saving = false;

  form = this.fb.nonNullable.group({
    methods: this.fb.nonNullable.control<string[]>([], {
      validators: [Validators.required],
    }),
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
        const current = res.activeMethods ?? [];
        this.form.controls.methods.setValue([...current]);
        this.form.markAsPristine();
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Failed to load merchant.');
      },
    });
  }

  isSameAsInitial(): boolean {
    const initial = this.merchant()?.activeMethods ?? [];
    const current = this.form.controls.methods.value;
    
    if (initial.length !== current.length) return false;
    
    const sortedInitial = [...initial].sort();
    const sortedCurrent = [...current].sort();
    
    return sortedInitial.every((val, index) => val === sortedCurrent[index]);
  }

  isSelected(method: string): boolean {
    return this.form.controls.methods.value.includes(method);
  }

  toggleMethod(method: string): void {
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
    if (!selected || selected.length === 0) return;

    this.saving = true;
    const payload: UpdateMethodsPayload = { methods: selected };

    this.merchantApi.updateMethods(this.merchantKey, payload).subscribe({
      next: (updatedNames) => {
        this.saving = false;
        this.successMessage.set('Payment methods updated successfully.');
        this.form.controls.methods.setValue([...updatedNames]);
        this.form.markAsPristine();

        const currentMerchant = this.merchant();
        if (currentMerchant) {
          this.merchant.set({ ...currentMerchant, activeMethods: updatedNames });
        }
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage.set(err?.error?.message ?? 'Failed to save payment methods.');
      },
    });
  }
}