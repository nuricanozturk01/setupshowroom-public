import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {
  ApiResponse, FavoriteProductGroup,
  ProfileInfo,
  SystemSpecs,
  UserInfo,
  UserProfileForm
} from '../model/profile.model';
import {Observable} from 'rxjs';
import {jwtDecode, JwtPayload} from 'jwt-decode';
import {environment} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private readonly API_URL = environment.apiUrl;
  constructor(private http: HttpClient) {
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  updateProfile(profile: UserProfileForm): Observable<ApiResponse<UserInfo>> {
    const userId = this.getUserId();
    return this.http.put<ApiResponse<UserInfo>>(`${this.API_URL}/api/user/${userId}`, profile, {headers: this.getAuthHeaders()});
  }

  getProfile(): Observable<ApiResponse<ProfileInfo>> {
    const userId = this.getUserId();
    return this.http.get<ApiResponse<ProfileInfo>>(`${this.API_URL}/api/user/${userId}/profile`, {headers: this.getAuthHeaders()});
  }

  upsertSystemSpecs(formData: FormData): Observable<ApiResponse<ProfileInfo>> {
    const userId = this.getUserId();
    return this.http.post<ApiResponse<ProfileInfo>>(
      `${this.API_URL}/api/user/${userId}/profile/system-requirements`,
      formData,
      {headers: this.getAuthHeaders()}
    );
  }

  saveFavoriteProductGroups(groups: any): Observable<ApiResponse<any>> {
    const userId = this.getUserId();

    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/user/${userId}/profile/product-group`, groups,
      {headers: this.getAuthHeaders()});
  }

  updateDisplayedGroups(): Observable<ApiResponse<any>> {
    const userId = this.getUserId();
    return this.http.get<ApiResponse<any>>(`${this.API_URL}/api/user/${userId}/profile/product-group`,
      {headers: this.getAuthHeaders()});
  }

  private getUserId(): string {
    const token = localStorage.getItem('token') || '';
    const decoded = jwtDecode<JwtPayload>(token);
    return <string>decoded.sub;
  }

  getUserProfile(username: string) {
    return this.http.get<ApiResponse<ProfileInfo>>(`${this.API_URL}/api/user/${username}/profile/info`, {headers: this.getAuthHeaders()});
  }

  changePassword(newPassword: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/user/${this.getUserId()}/reset-password`, newPassword, {
      headers: this.getAuthHeaders()
    });
  }
}
