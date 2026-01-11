import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { TokenService } from './token';

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const token = tokenService.getToken();

  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    })
  );
};
