import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../../core/services/auth-api/auth-api';
import { TokenService } from '../../../core/services/token/token';
import { LoginRequest, MfaVerificationRequest } from '../../../shared/models/auth';

@Component({
  selector: 'app-psp-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './psp-login.html',
  styleUrl: './psp-login.scss',
})
export class PspLogin {
  error = signal<string | null>(null);
  loading = signal<boolean>(false);
  showMfa = signal<boolean>(false);
  userEmail = signal<string>('');
  
  otpSteps = [0, 1, 2, 3, 4, 5];
  otpValues = signal<string[]>(['', '', '', '', '', '']);

  loginForm;

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private tokenService: TokenService,
    private router: Router
  ) {
    this.loginForm = this.fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  onOtpInput(event: any, index: number): void {
    const value = event.target.value;
    if (value.length > 1) {
      event.target.value = value.charAt(0);
      return;
    }

    const currentValues = [...this.otpValues()];
    currentValues[index] = value;
    this.otpValues.set(currentValues);

    if (value && index < 5) {
      const nextInput = event.target.nextElementSibling as HTMLInputElement;
      if (nextInput) nextInput.focus();
    }
  }

  onKeyDown(event: KeyboardEvent, index: number): void {
    if (event.key === 'Backspace' && !this.otpValues()[index] && index > 0) {
      const prevInput = (event.target as HTMLInputElement).previousElementSibling as HTMLInputElement;
      if (prevInput) prevInput.focus();
    }
  }

  submitLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.error.set(null);
    this.loading.set(true);
    const payload: LoginRequest = this.loginForm.getRawValue();
    this.authApi.login(payload).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.mfaRequired) {
          this.userEmail.set(res.email);
          this.showMfa.set(true);
        } else {
          this.tokenService.setToken(res.token);
          this.router.navigate(['/merchants']);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.handleError(err);
      },
    });
  }

  submitMfa(): void {
    const code = this.otpValues().join('');
    if (code.length < 6) {
      this.error.set('Please enter the full 6-digit code.');
      return;
    }

    this.error.set(null);
    this.loading.set(true);

    const payload: MfaVerificationRequest = {
      email: this.userEmail(),
      code: code
    };

    this.authApi.verifyMfa(payload).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.tokenService.setToken(res.token);
        this.router.navigate(['/merchants']);
      },
      error: (err) => {
        this.loading.set(false);
        this.handleError(err);
      }
    });
  }

  private handleError(err: any): void {
    if (err.status === 401 || err.status === 403) {
      this.error.set('Invalid credentials or verification code.');
    } else {
      this.error.set(err.error?.message || 'An unexpected error occurred.');
    }
  }
}