import { TestBed } from '@angular/core/testing';
import { TokenService } from './token';

describe('TokenService', () => {
  let service: TokenService;
  const TOKEN_KEY = 'psp_auth_token';

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenService);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should store token in localStorage', () => {
    service.setToken('test-token');
    expect(localStorage.getItem(TOKEN_KEY)).toBe('test-token');
  });

  it('should get token from localStorage', () => {
    localStorage.setItem(TOKEN_KEY, 'stored-token');
    expect(service.getToken()).toBe('stored-token');
  });

  it('should clear token from localStorage', () => {
    localStorage.setItem(TOKEN_KEY, 'stored-token');
    service.clear();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('should return true if user is logged in', () => {
    localStorage.setItem(TOKEN_KEY, 'stored-token');
    expect(service.isLoggedIn()).toBe(true);
  });

  it('should return false if user is not logged in', () => {
    localStorage.removeItem(TOKEN_KEY);
    expect(service.isLoggedIn()).toBe(false);
  });
});
