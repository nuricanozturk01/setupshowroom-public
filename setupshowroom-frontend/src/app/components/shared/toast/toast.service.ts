import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Toast} from './toast-model';

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  private toasts$ = new BehaviorSubject<Toast[]>([]);
  private defaultDuration = 3000;
  private counter = 0;

  getToasts() {
    return this.toasts$.asObservable();
  }

  success(message: string, duration?: number): void {
    this.show(message, 'success', duration);
  }

  error(message: string, duration?: number): void {
    this.show(message, 'error', duration);
  }

  warning(message: string, duration?: number): void {
    this.show(message, 'warning', duration);
  }

  info(message: string, duration?: number): void {
    this.show(message, 'info', duration);
  }

  private show(message: string, type: Toast['type'], duration?: number): void {
    const id = ++this.counter;
    const toast: Toast = {
      id,
      message,
      type,
      duration: duration || this.defaultDuration
    };

    this.toasts$.next([...this.toasts$.value, toast]);

    setTimeout(() => {
      this.remove(id);
    }, toast.duration);
  }

  private remove(id: number): void {
    const currentToasts = this.toasts$.value;
    this.toasts$.next(currentToasts.filter(t => t.id !== id));
  }
}
