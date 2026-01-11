import { CommonModule } from '@angular/common';
import { Component, effect, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  FormControl,
  FormGroup,
} from '@angular/forms';
import { MerchantApi } from '../../../../core/services/merchant-api/merchant-api';
import { finalize } from 'rxjs';

type MerchantDetailsView = {
  id: number;
  name: string;
  merchantId: string;
  activeMethods: string[];
};

const ALL_METHODS = ['CARD', 'QR', 'PAYPAL', 'CRYPTO'] as const;
type PaymentMethod = (typeof ALL_METHODS)[number];

@Component({
  selector: 'app-psp-merchant-details',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './merchant-details.html',
  styleUrl: './merchant-details.scss',
})
export class MerchantDetails {
  // ===== signals =====
  loading = signal(true);
  saving = signal(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  merchant = signal<MerchantDetailsView | null>(null);

  methods = ALL_METHODS;

  // ✅ T A Č N O tipizirana forma (da TS vidi selectedMethods)
  form!: FormGroup<{
    selectedMethods: FormControl<PaymentMethod[]>;
  }>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private merchantApi: MerchantApi
  ) {
    // ✅ forma MORA ovde (ne van konstruktora)
    this.form = this.fb.nonNullable.group({
      selectedMethods: this.fb.nonNullable.control<PaymentMethod[]>([], {
        validators: [Validators.required],
      }),
    }) as FormGroup<{
      selectedMethods: FormControl<PaymentMethod[]>;
    }>;

    // ✅ sync forme kad se merchant učita/promeni
    effect(() => {
      const m = this.merchant();
      if (!m) return;

      this.form.controls.selectedMethods.setValue(
        m.activeMethods as PaymentMethod[]
      );
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    // TODO: kasnije GET /api/merchants/:id
    const mock: MerchantDetailsView = {
      id: Number.isNaN(id) ? 1 : id,
      name: 'Rent-a-car Novi Sad',
      merchantId: 'MERCH_001',
      activeMethods: ['CARD', 'QR'],
    };

    this.merchant.set(mock);
    this.loading.set(false);
  }

  toggle(method: PaymentMethod): void {
    const current = this.form.controls.selectedMethods.value;
    const exists = current.includes(method);

    const next = exists
      ? current.filter(m => m !== method)
      : [...current, method];

    this.form.controls.selectedMethods.setValue(next);
    this.form.controls.selectedMethods.markAsTouched();
  }

  isChecked(method: PaymentMethod): boolean {
    return this.form.controls.selectedMethods.value.includes(method);
  }

  save(): void {
    this.error.set(null);
    this.success.set(null);

    const selected = this.form.controls.selectedMethods.value;

    if (!selected || selected.length === 0) {
      this.error.set('You must keep at least one payment method active.');
      return;
    }

    const m = this.merchant();
    if (!m) {
      this.error.set('Merchant not loaded.');
      return;
    }

    this.saving.set(true);

    const payload = { methods: selected };

    this.merchantApi
      .updateMethods(m.merchantId, payload)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.success.set('Payment methods updated.');
          this.merchant.update(prev =>
            prev ? { ...prev, activeMethods: [...selected] } : prev
          );
        },
        error: (err) => {
          console.error('UPDATE METHODS error:', err);
          this.error.set(err?.error?.message ?? 'Save failed.');
        },
      });
  }
}
