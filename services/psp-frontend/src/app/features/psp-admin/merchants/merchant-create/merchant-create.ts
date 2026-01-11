import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

type CreateMerchantPayload = {
  name: string;
  merchantId: string;
  merchantPassword: string;
  successUrl: string;
  failedUrl: string;
  errorUrl: string;
};

@Component({
  selector: 'app-psp-merchant-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './merchant-create.html',
  styleUrl: './merchant-create.scss',
})
export class MerchantCreate {
  error: string | null = null;
  loading = false;

  form;

  constructor(private fb: FormBuilder, private router: Router) {
    this.form = this.fb.nonNullable.group({
      name: ['', Validators.required],
      merchantId: ['', Validators.required],
      merchantPassword: ['', Validators.required],
      successUrl: ['', [Validators.required]],
      failedUrl: ['', [Validators.required]],
      errorUrl: ['', [Validators.required]],
    });
  }

  submit(): void {
    this.error = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const payload: CreateMerchantPayload = this.form.getRawValue();

    // TODO: kasnije zameniti API pozivom (POST /api/merchants)
    console.log('CREATE MERCHANT PAYLOAD:', payload);

    // simulacija uspeha
    setTimeout(() => {
      this.loading = false;
      this.router.navigate(['/psp/merchants']);
    }, 400);
  }
}
