import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MerchantApi } from '../../../../core/services/merchant-api/merchant-api';
import { MerchantCreateRequest } from '../../../../shared/models/merchant';

@Component({
  selector: 'app-merchant-create',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './merchant-create.html',
  styleUrl: './merchant-create.scss',
})
export class MerchantCreate {
  error: string | null = null;
  loading = false;

  form;

  constructor(
    private fb: FormBuilder,
    private merchantApi: MerchantApi,
    private router: Router
  ) {
    this.form = this.fb.nonNullable.group({
      merchantKey: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(64),
          Validators.pattern(/^[A-Za-z0-9._-]+$/),
        ],
      ],
    });
  }

  submit(): void {
    this.error = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const payload: MerchantCreateRequest = this.form.getRawValue();

    this.merchantApi.create(payload).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/merchants']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Merchant creation failed.';
      },
    });
  }
}
