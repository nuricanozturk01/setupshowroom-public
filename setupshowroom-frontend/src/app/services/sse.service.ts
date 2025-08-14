import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { getUserId } from '../utils/util';
import { ToastService } from '../components/shared/toast/toast.service';
import { environment } from '../../environments/environment';
import { Notification } from '../components/notifications/model/notification.model';

@Injectable({
  providedIn: 'root'
})
export class SseService {
  private eventSource?: EventSource;
  private messageSubject = new BehaviorSubject<Notification | null>(null);
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts = 5;
  private readonly reconnectDelay = 3000;
  private isConnecting = false;
  private currentUserId: string | null = null;

  constructor(private toastService: ToastService) {}

  connect(): void {
    if (this.isConnecting) {
      //console.log('SSE connection already in progress');
      return;
    }

    if (this.eventSource && this.eventSource.readyState === EventSource.OPEN) {
      //console.log('SSE connection already open');
      return;
    }

    // Clean up any existing connection
    this.disconnect();

    const userId = getUserId();
    if (!userId) {
      console.error('Cannot connect to SSE: No user ID available');
      return;
    }

    this.currentUserId = userId;
    const url = `${environment.apiUrl}/public/notification/stream?userId=${userId}`;

    try {
      this.isConnecting = true;
      //console.log('Attempting to establish SSE connection:', url);

      this.eventSource = new EventSource(url);

      this.setupEventListeners();
      this.reconnectAttempts = 0;
    } catch (error) {
      console.error('Error creating SSE connection:', error);
      this.isConnecting = false;
      this.handleReconnect();
    }
  }

  private setupEventListeners(): void {
    if (!this.eventSource) {
      console.error('Cannot setup event listeners: No event source available');
      return;
    }

    this.eventSource.onopen = () => {
      this.isConnecting = false;
      this.reconnectAttempts = 0;
      //console.log('SSE connection opened successfully for user:', this.currentUserId);
    };

    this.eventSource.addEventListener('INIT', (event) => {
      //console.log('SSE INIT event received:', event.data);
    });

    this.eventSource.addEventListener('notification', (event) => {
      //console.log('Raw SSE notification event:', event);
      try {
        const data: Notification = JSON.parse(event.data);
        //console.log('Parsed SSE notification:', data);

        if (data && data.user && data.user.id === this.currentUserId) {
          if (data.description) {
            this.toastService.info(data.description);
          }
          this.messageSubject.next(data);
        } else {
          /*console.log('Notification not for current user:', {
            currentUserId: this.currentUserId,
            notificationUserId: data?.user?.id
          });*/
        }
      } catch (error) {
        console.error('Error processing notification event:', error);
      }
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE connection error details:', {
        readyState: this.eventSource?.readyState,
        error: error,
        userId: this.currentUserId,
        url: this.eventSource?.url
      });
      this.isConnecting = false;
      this.handleReconnect();
    };
  }

  private handleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      //console.log('Max reconnection attempts reached, disconnecting');
      this.disconnect();
      return;
    }

    this.reconnectAttempts++;
    //console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = undefined;
    }

    setTimeout(() => {
      this.connect();
    }, this.reconnectDelay);
  }

  public disconnect(): void {
    if (this.eventSource) {
      //console.log('SSE connection closed for user:', this.currentUserId);
      //console.log('SSE connection state before close:', this.eventSource.readyState);
      this.eventSource.close();
      this.eventSource = undefined;
      this.isConnecting = false;
      this.currentUserId = null;
      this.reconnectAttempts = 0;
    }
  }

  public onMessage(): BehaviorSubject<Notification | null> {
    return this.messageSubject;
  }
}
