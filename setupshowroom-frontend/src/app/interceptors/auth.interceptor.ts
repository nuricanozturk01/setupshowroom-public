import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../components/auth/service/auth.service';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { Router } from '@angular/router';

// Singleton to track refresh token state
let isRefreshing = false;
let refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.accessToken;

  // Skip auth header for refresh token request and login
  if (req.url.includes('refresh-token') || req.url.includes('login') || req.url.includes('register')) {
    return next(req);
  }

  if (!token) {
    handleAuthError(router);
    return throwError(() => new Error('No token available'));
  }

  try {
    const decodedToken = jwtDecode<any>(token);
    const currentTime = Date.now() / 1000;
    const tokenExpiration = decodedToken.exp;
    const timeUntilExpiration = tokenExpiration - currentTime;

    // If token will expire in less than 30 seconds, try to refresh it
    if (timeUntilExpiration < 120) {
      if (!isRefreshing) {
        isRefreshing = true;
        refreshTokenSubject.next(null);

        return authService.refreshToken().pipe(
          switchMap(success => {
            isRefreshing = false;
            const newToken = authService.accessToken;
            if (!newToken) {
              handleAuthError(router);
              return throwError(() => new Error('Token refresh failed'));
            }
            refreshTokenSubject.next(newToken);
            return next(addTokenToRequest(req, newToken));
          }),
          catchError(error => {
            isRefreshing = false;
            handleAuthError(router);
            return throwError(() => error);
          })
        );
      }

      // Wait for the token to be refreshed
      return refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(newToken => {
          return next(addTokenToRequest(req, newToken!));
        })
      );
    }

    // Add current token to request
    return next(addTokenToRequest(req, token)).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          if (!isRefreshing) {
            isRefreshing = true;
            refreshTokenSubject.next(null);

            return authService.refreshToken().pipe(
              switchMap(success => {
                isRefreshing = false;
                const newToken = authService.accessToken;
                if (!newToken) {
                  handleAuthError(router);
                  return throwError(() => new Error('Token refresh failed'));
                }
                refreshTokenSubject.next(newToken);
                return next(addTokenToRequest(req, newToken));
              }),
              catchError(refreshError => {
                isRefreshing = false;
                handleAuthError(router);
                return throwError(() => refreshError);
              })
            );
          }

          // Wait for the token to be refreshed
          return refreshTokenSubject.pipe(
            filter(token => token !== null),
            take(1),
            switchMap(newToken => {
              return next(addTokenToRequest(req, newToken!));
            })
          );
        }
        return throwError(() => error);
      })
    );
  } catch (error) {
    console.error('Token decode error:', error);
    handleAuthError(router);
    return throwError(() => error);
  }
};

function addTokenToRequest(request: HttpRequest<any>, token: string): HttpRequest<any> {
  return request.clone({
    headers: request.headers.set('Authorization', `Bearer ${token}`)
  });
}

function handleAuthError(router: Router): void {
  // Clear tokens and redirect to login
  localStorage.removeItem('token');
  localStorage.removeItem('refresh_token');
  router.navigate(['/login'], {
    queryParams: { returnUrl: router.routerState.snapshot.url }
  });
}
