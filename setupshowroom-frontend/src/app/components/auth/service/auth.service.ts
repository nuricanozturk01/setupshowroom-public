import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, of, map, catchError} from 'rxjs';
import {Router} from '@angular/router';
import {environment} from '../../../../environments/environment';
import {ApiResponse, UserInfo} from '../../profile/model/profile.model';
import {jwtDecode, JwtPayload} from 'jwt-decode';
import {AuthResponse, CustomJwtPayload, LoginForm, RegisterForm} from '../auth-model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  private readonly ACCESS_TOKEN_KEY = 'token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly EMAIL_KEY = 'email';

  constructor(private http: HttpClient, private router: Router) {
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  login(loginForm: LoginForm): Observable<ApiResponse<AuthResponse>> {
    try {
      return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/api/auth/login`, loginForm)
    } catch (error) {
      console.error('Error logging in', error);
      throw error;
    }
  }

  register(userData: RegisterForm): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/api/auth/register`, userData)
  }

  logout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  get accessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  get refreshTokenValue(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  public setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    const emailClaim = this.getEmailClaim(accessToken);

    if (emailClaim) {
      localStorage.setItem(this.EMAIL_KEY, emailClaim);
    }
  }

  refreshToken(): Observable<boolean> {
    const refreshToken = this.refreshTokenValue;

    const request = {
      refresh_token: refreshToken
    }

    if (!refreshToken) {
      return of(false);
    }

    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/api/auth/refresh-token`, request).pipe(
      map(response => {
        if (response.success && response.data) {
          this.setTokens(response.data.token, response.data.refresh_token);
          return true;
        }
        return false;
      }),
      catchError(error => {
        console.error('Refresh token error:', error);
        this.logout();
        return of(false);
      })
    );
  }

  private getUserId(): string {
    const token = localStorage.getItem('token') || '';
    const decoded = jwtDecode<JwtPayload>(token);
    return <string>decoded.sub;
  }

  getUserInfo(): Observable<ApiResponse<UserInfo>> {
    const headers = this.getAuthHeaders();

    return this.http.get<ApiResponse<UserInfo>>(`${this.API_URL}/api/user/${this.getUserId()}`, {headers})
  }

  loginOauth(token: string, refresh_token: string) {
    this.setTokens(token, refresh_token);
  }

  private getEmailClaim(accessToken: string) {
    try {
      const decodedToken: CustomJwtPayload = jwtDecode<JwtPayload>(accessToken);
      return decodedToken.email || null;
    } catch (error) {
      console.error('Error decoding token', error);
      return null;
    }
  }
}
