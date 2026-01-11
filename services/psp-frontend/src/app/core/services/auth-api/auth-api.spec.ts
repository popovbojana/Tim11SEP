import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuthApiService, LoginRequest } from './auth-api';
import { environment } from '../../../../environments/environment';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthApiService],
    });

    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call POST /api/auth/login and return token', () => {
    const mockPayload: LoginRequest = {
      email: 'test@test.com',
      password: 'password123',
    };

    const mockResponse = {
      token: 'fake-jwt-token',
    };

    service.login(mockPayload).subscribe((res) => {
      expect(res.token).toBe(mockResponse.token);
    });

    const req = httpMock.expectOne(
      `${environment.apiBaseUrl}/api/auth/login`
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockPayload);

    req.flush(mockResponse);
  });
});
