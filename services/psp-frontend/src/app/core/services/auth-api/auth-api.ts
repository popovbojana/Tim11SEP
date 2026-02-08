import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LoginRequest, AuthResponse, MfaVerificationRequest } from '../../../shared/models/auth';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.baseUrl}/api/auth/login`,
      payload
    );
  }

  verifyMfa(payload: MfaVerificationRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.baseUrl}/api/auth/verify-mfa`,
      payload
    );
  }
}