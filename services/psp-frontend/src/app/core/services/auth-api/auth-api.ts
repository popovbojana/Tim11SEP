import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type LoginRequest = {
  email: string;
  password: string;
};

export type AuthResponse = {
  token: string;
};

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  // PSP backend base URL
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.baseUrl}/api/auth/login`,
      payload
    );
  }
}
