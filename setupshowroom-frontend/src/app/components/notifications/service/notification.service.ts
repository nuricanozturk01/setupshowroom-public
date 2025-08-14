import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject, takeUntil} from 'rxjs';
import {Notification} from '../model/notification.model';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {ApiResponse} from '../../profile/model/profile.model';
import {jwtDecode, JwtPayload} from 'jwt-decode';
import {environment} from '../../../../environments/environment';
import {SseService} from '../../../services/sse.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications = new BehaviorSubject<Notification[]>([]);
  private unreadCount = new BehaviorSubject<number>(0);
  private destroy$ = new Subject<void>();
  private isInitialized = false;

  constructor(
    private sseService: SseService,
    private http: HttpClient
  ) {
    this.initializeSSE();
  }

  private initializeSSE(): void {
    if (this.isInitialized) {
      //console.log('SSE already initialized in NotificationService');
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      console.error('Cannot initialize SSE: No authentication token available');
      return;
    }

    try {
      const userId = this.getUserId();
      if (!userId) {
        console.error('Cannot initialize SSE: No user ID available');
        return;
      }

      //console.log('Initializing SSE connection for user:', userId);
      this.sseService.connect();

      this.sseService.onMessage()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (notification) => {
            if (notification) {
              //console.log('Received new notification:', notification);
              const currentNotifications = this.notifications.value;
              this.notifications.next([notification, ...currentNotifications]);
              this.updateUnreadCount();
            }
          },
          error: (error) => {
            console.error('Error in SSE subscription:', error);
            this.isInitialized = false; // Reset initialization state on error
          }
        });

      this.isInitialized = true;
    } catch (error) {
      console.error('Error initializing SSE:', error);
      this.isInitialized = false;
    }
  }

  public getNotifications$(): Observable<Notification[]> {
    return this.notifications.asObservable();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({'Authorization': `Bearer ${token}`});
  }

  getUnreadCount(): Observable<number> {
    return this.unreadCount.asObservable();
  }

  public loadNotifications(page: number = 0, size: number = 20, append: boolean = false): Observable<boolean> {
    const userId = this.getUserId();
    if (!userId) return new BehaviorSubject(false).asObservable();

    const headers = this.getAuthHeaders();
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    const result$ = new BehaviorSubject<boolean>(false);

    this.http.get<ApiResponse<Notification[]>>(`${environment.apiUrl}/api/v1/user/${userId}/notification/all`, {
      headers,
      params
    })
      .subscribe({
        next: (response) => {
          if (response.success && response.status == 200) {
            const data = response.data;
            const currentNotifications = this.notifications.value;
            const merged = append ? [...currentNotifications, ...data] : data;
            this.notifications.next(merged);
            this.updateUnreadCount();
            result$.next(data.length < size);
          }
        },
        error: (err) => console.error('Error loading notifications', err),
      });

    return result$.asObservable();
  }

  markAsRead(notificationId: string) {
    this.http.post<ApiResponse<Notification>>(
      `${environment.apiUrl}/api/v1/user/${this.getUserId()}/notification/${notificationId}/read`, {}, {headers: this.getAuthHeaders()}
    ).subscribe(response => {
      if (response.success) {
        const updated = this.notifications.value.map(n => n.id === notificationId ? {...n, read: true} : n);
        this.notifications.next(updated);
        this.updateUnreadCount();
      }
    });
  }

  markAllAsRead() {
    this.http.post<ApiResponse<any>>(
      `${environment.apiUrl}/api/v1/user/${this.getUserId()}/notification/read-all`, {}, {headers: this.getAuthHeaders()}
    ).subscribe(response => {
      if (response.success) {
        const updated = this.notifications.value.map(n => ({...n, read: true}));
        this.notifications.next(updated);
        this.updateUnreadCount();
      }
    });
  }

  private updateUnreadCount() {
    const count = this.notifications.value.filter(n => !n.read).length;
    this.unreadCount.next(count);
  }

  private getUserId(): string {
    const token = localStorage.getItem('token') || '';
    const decoded = jwtDecode<JwtPayload>(token);
    return <string>decoded.sub;
  }
}
