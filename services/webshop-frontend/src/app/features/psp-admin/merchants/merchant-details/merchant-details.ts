import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

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
  loading = true;
  saving = false;
  error: string | null = null;
  success: string | null = null;

  merchant: MerchantDetailsView | null = null;
  methods = ALL_METHODS;

  form;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.form = this.fb.nonNullable.group({
      selectedMethods: this.fb.nonNullable.control<PaymentMethod[]>([], {
        validators: [Validators.required],
      }),
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    // TODO: kasnije GET /api/merchants/:id
    // mock podaci (možeš proširiti)
    const mock: MerchantDetailsView = {
      id: Number.isNaN(id) ? 1 : id,
      name: 'Rent-a-car Novi Sad',
      merchantId: 'MERCH_001',
      activeMethods: ['CARD', 'QR'],
    };

    this.merchant = mock;
    this.form.controls.selectedMethods.setValue(mock.activeMethods as PaymentMethod[]);
    this.loading = false;
  }

  toggle(method: PaymentMethod): void {
    const current = this.form.controls.selectedMethods.value;
    const exists = current.includes(method);

    const next = exists ? current.filter(m => m !== method) : [...current, method];

    this.form.controls.selectedMethods.setValue(next);
    this.form.controls.selectedMethods.markAsTouched();
  }

  isChecked(method: PaymentMethod): boolean {
    return this.form.controls.selectedMethods.value.includes(method);
  }

  save(): void {
    this.error = null;
    this.success = null;

    const selected = this.form.controls.selectedMethods.value;

    if (!selected || selected.length === 0) {
      this.error = 'You must keep at least one payment method active.';
      return;
    }

    this.saving = true;

    // Ovo je TAČAN shape koji backend očekuje:
    const payload = { methods: selected };

    // TODO: kasnije PUT/PATCH na endpoint iz MerchantController-a
    console.log('UPDATE METHODS payload:', payload);

    setTimeout(() => {
      this.saving = false;
      this.success = 'Payment methods updated.';
      // opcionalno: update lokalnog modela
      if (this.merchant) this.merchant.activeMethods = [...selected];
    }, 400);
  }
}
