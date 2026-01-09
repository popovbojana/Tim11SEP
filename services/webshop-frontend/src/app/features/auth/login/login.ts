import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService, LoginRequest } from '../../../core/services/auth-api/auth-api';
import { TokenService } from '../../../core/services/token/token';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
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
    this.error = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const payload: LoginRequest = this.form.getRawValue();

    this.authApi.login(payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.tokenService.setToken(res.token);
        this.router.navigate(['/offers']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Login failed.';
      },
    });
  }
}
