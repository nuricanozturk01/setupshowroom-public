import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {jwtDecode, JwtPayload} from 'jwt-decode';
import {ApiResponse} from '../../profile/model/profile.model';
import {CommentForm, CommentInfo, SetupInfo, SetupUpdateForm} from '../setup-model';
import {environment} from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SetupService {

  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  createSetup(formData: FormData): Observable<ApiResponse<SetupInfo>> {
    const headers = this.getAuthHeaders();
    return this.http.post<ApiResponse<SetupInfo>>(`${this.API_URL}/api/setup`, formData, {headers});
  }

  createSetupByUserProfile(formData: FormData): Observable<ApiResponse<SetupInfo>> {
    const headers = this.getAuthHeaders();
    return this.http.post<ApiResponse<SetupInfo>>
    (`${this.API_URL}/api/setup/${this.getUserId()}/setup-by-profile`, formData, {headers});
  }


  private getUserId(): string {
    const token = localStorage.getItem('token') || '';
    const decoded = jwtDecode<JwtPayload>(token);
    return <string>decoded.sub;
  }

  findAllByUserIdPageable(page: number = 0, size: number = 2): Observable<ApiResponse<SetupInfo[]>> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<SetupInfo[]>>(`${this.API_URL}/api/setup/${this.getUserId()}/setups`, {
      headers,
      params
    });
  }

  findCommentsBySetupId(id: string, currentPage: number, pageSize: number): Observable<ApiResponse<CommentInfo[]>> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams()
      .set('page', currentPage.toString())
      .set('size', pageSize.toString());

    return this.http.get<ApiResponse<CommentInfo[]>>(`${this.API_URL}/api/setup/${id}/comments`, {
      headers,
      params
    });
  }

  postComment(id: string, commentForm: CommentForm): Observable<ApiResponse<CommentInfo>> {
    const headers = this.getAuthHeaders();

    return this.http.post<ApiResponse<CommentInfo>>(`${this.API_URL}/api/setup/${id}/comment`, commentForm, {
      headers
    });
  }

  deleteComment(setupId: string, commentId: string): Observable<ApiResponse<void>> {
    const headers = this.getAuthHeaders();
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/api/setup/${setupId}/comment/${commentId}`, {
      headers
    });
  }

  updateComment(setupId: string, commentId: string, commentForm: CommentForm): Observable<ApiResponse<CommentInfo>> {
    const headers = this.getAuthHeaders();
    return this.http.put<ApiResponse<CommentInfo>>(
      `${this.API_URL}/api/setup/${setupId}/comment/${commentId}`,
      commentForm,
      { headers }
    );
  }

  reportComment(reportForm: any): Observable<ApiResponse<any>> {
    const headers = this.getAuthHeaders();
    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/report`, reportForm, {
      headers
    });
  }

  reportSetup(reportForm: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/report`, reportForm);
  }

  likeSetup(id: string): Observable<ApiResponse<any>> {
    const headers = this.getAuthHeaders();

    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/setup/${id}/like`, {
      headers
    });
  }

  unlikeSetup(id: string): Observable<ApiResponse<any>> {
    const headers = this.getAuthHeaders();

    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/setup/${id}/unlike`, {
      headers
    });
  }

  addFavorite(id: string) {
    const headers = this.getAuthHeaders();

    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/setup/${id}/favorite`, {
      headers
    });
  }

  removeFavorite(id: string) {
    const headers = this.getAuthHeaders();

    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/setup/${id}/unfavorite`, {
      headers
    });
  }

  deleteSetup(setupId: string): Observable<ApiResponse<SetupInfo>> {
    const headers = this.getAuthHeaders();

    return this.http.delete<ApiResponse<SetupInfo>>(`${this.API_URL}/api/setup/${setupId}`, {
      headers
    });
  }

  updateSetup(setupId: string, formData: FormData, setupData: SetupUpdateForm): Observable<ApiResponse<SetupInfo>> {
    return this.http.put<ApiResponse<SetupInfo>>(`${this.API_URL}/api/setup/${this.getUserId()}/setup/${setupId}`, formData, {})
  }

  getSetupById(setupId: string): Observable<ApiResponse<SetupInfo>> {
    const headers = this.getAuthHeaders();
    return this.http.get<ApiResponse<SetupInfo>>(`${this.API_URL}/api/setup/${this.getUserId()}/setups/${setupId}`, {headers});
  }

  getSetupByUserId(id: string, page: number = 0, size: number = 2): Observable<ApiResponse<SetupInfo[]>> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<SetupInfo[]>>(`${this.API_URL}/api/user/${id}/setups`, {headers, params});
  }

  likeComment(setupId: string, commentId: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API_URL}/api/setup/${this.getUserId()}/setups/${setupId}/comments/${commentId}/like`, {
      headers: this.getAuthHeaders()
    });
  }

  unlikeComment(setupId: string, commentId: string): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.API_URL}/api/setup/${this.getUserId()}/setups/${setupId}/comments/${commentId}/unlike`,
      {headers: this.getAuthHeaders()});
  }

  uploadImage(formData: FormData): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.API_URL}/api/setups/upload`, formData);
  }

  getCardImages(): Observable<ApiResponse<string[]>> {
    const headers = this.getAuthHeaders();
    return this.http.get<ApiResponse<string[]>>(`${this.API_URL}/card/list`, { headers });
  }
}
