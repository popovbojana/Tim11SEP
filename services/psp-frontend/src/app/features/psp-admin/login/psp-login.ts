import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService, LoginRequest } from '../../../core/services/auth-api/auth-api';
import { TokenService } from '../../../core/services/token/token';

@Component({
  selector: 'app-psp-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './psp-login.html',
  styleUrl: './psp-login.scss',
})
export class PspLogin {
  error: string | null = null;
  loading = false;

  form;

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private tokenService: TokenService,
    private router: Router
  ) {
    this.form = this.fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.error = null;
    this.loading = true;

    const payload: LoginRequest = this.form.getRawValue();

    this.authApi.login(payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.tokenService.setToken(res.token);
        this.router.navigate(['/merchants']);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 403 || err.status === 401) {
          this.error = 'Invalid email or password.';
        } else if (err.status === 500) {
          this.error = 'Internal server error. Please try again later.';
        } else if (err.error?.message) {
          this.error = err.error.message;
        } else {
          this.error = 'An unexpected error occurred.';
        }
        
        console.error('Login failed:', err);
      },
    });
  }
}