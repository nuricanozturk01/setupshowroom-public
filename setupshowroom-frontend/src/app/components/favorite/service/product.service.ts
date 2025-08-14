import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  Product,
  ProductGroup,
  CreateProductGroupRequest,
  CreateProductRequest,
  UpdateProductGroupRequest,
  UpdateProductRequest
} from '../../profile/model/product.model';
import {ApiResponse} from '../../profile/model/profile.model';
import {environment} from '../../../../environments/environment';
import {jwtDecode, JwtPayload} from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  // Product Group Operations
  getProductGroups(): Observable<ApiResponse<ProductGroup[]>> {
    return this.http.get<ApiResponse<ProductGroup[]>>(`${this.apiUrl}/groups`);
  }

  createProductGroup(request: CreateProductGroupRequest): Observable<ApiResponse<ProductGroup>> {
    const userId = this.getUserId();

    return this.http.post<ApiResponse<ProductGroup>>
    (`${this.apiUrl}/api/user/${userId}/profile/favorite-products/group`,
      request,
      {headers: this.getAuthHeaders()});
  }

  updateProductGroup(groupId: string, request: UpdateProductGroupRequest): Observable<ApiResponse<string>> {
    const userId = this.getUserId();
    return this.http.put<ApiResponse<string>>(
      `${this.apiUrl}/api/user/${userId}/profile/favorite-products/group/${groupId}`,
      request,
      {headers: this.getAuthHeaders()}
    );
  }

  deleteProductGroup(groupId: string): Observable<ApiResponse<any>> {
    const userId = this.getUserId();

    return this.http.delete<ApiResponse<Product>>
    (`${this.apiUrl}/api/user/${userId}/profile/favorite-products/group/${groupId}`,
      {headers: this.getAuthHeaders()});
  }

  // Product Operations
  createProduct(groupId: string, request: CreateProductRequest): Observable<ApiResponse<Product>> {
    const userId = this.getUserId();

    return this.http.post<ApiResponse<Product>>
    (`${this.apiUrl}/api/user/${userId}/profile/favorite-products/group/${groupId}`,
      request,
      {headers: this.getAuthHeaders()});
  }

  updateProduct(productId: string, groupId: string, product: CreateProductRequest): Observable<ApiResponse<Product>> {
    const userId = this.getUserId();
    return this.http.put<ApiResponse<Product>>(
      `${this.apiUrl}/api/user/${userId}/profile/favorite-products/group/${groupId}/product/${productId}`,
      product,
      {headers: this.getAuthHeaders()}
    );
  }

  deleteProduct(productId: string, groupId: string): Observable<ApiResponse<any>> {
    const userId = this.getUserId();

    return this.http.delete<ApiResponse<Product>>
    (`${this.apiUrl}/api/user/${userId}/profile/favorite-products/group/${groupId}/product/${productId}`,
      {headers: this.getAuthHeaders()});
  }

  private getUserId(): string {
    const token = localStorage.getItem('token') || '';
    const decoded = jwtDecode<JwtPayload>(token);
    return <string>decoded.sub;
  }
}
