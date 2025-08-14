import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {jwtDecode, JwtPayload} from 'jwt-decode';
import {Observable} from 'rxjs';
import {ApiResponse} from '../profile/model/profile.model';
import {SetupInfo} from '../setup/setup-model';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FavoriteService {

  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  private getUserId(): string {
    const token = localStorage.getItem('token') || '';
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return <string>decoded.sub;
    } catch (error) {
      console.error('Error decoding token:', error);
      return '';
    }
  }

  /**
   * Get paginated feed posts
   * @param page Page number (0-based)
   * @param size Number of items per page
   * @returns Observable with paginated feed posts
   */
  getFeedPageable(page: number = 0, size: number = 10): Observable<ApiResponse<SetupInfo[]>> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<SetupInfo[]>>(`${this.API_URL}/api/user/${this.getUserId()}/setups/favorite`, {
      headers,
      params
    });
  }
}
