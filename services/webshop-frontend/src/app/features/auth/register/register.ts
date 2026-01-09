import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService, RegisterRequest } from '../../../core/services/auth-api/auth-api';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  error: string | null = null;
  loading = false;

  form;

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private router: Router
  ) {
    this.form = this.fb.nonNullable.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  submit(): void {
    this.error = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const payload: RegisterRequest = this.form.getRawValue();

    this.authApi.register(payload).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Registration failed.';
      },
    });
  }
}
