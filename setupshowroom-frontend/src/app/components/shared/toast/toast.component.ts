import {Component, ChangeDetectionStrategy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ToastService} from './toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed top-4 right-4 z-[9999] flex flex-col gap-2"
      aria-live="polite"
      aria-atomic="true">
      @for (toast of toasts$ | async; track toast.id) {
        <div
          class="alert transition-all duration-300 ease-in-out"
          [class]="getAlertClass(toast.type)"
          role="alert">
          <div class="flex items-center gap-2">
            @switch (toast.type) {
              @case ('success') {
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5"
                     fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
              }
              @case ('error') {
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5"
                     fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              }
              @case ('warning') {
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5"
                     fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0
                    2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34
                    16c-.77 1.333.192 3 1.732 3z"/>
                </svg>
              }
              @case ('info') {
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5"
                     fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round"
                        stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18
                    0 9 9 0 0118 0z"/>
                </svg>
              }
            }
            <span>{{ toast.message }}</span>
          </div>
        </div>
      }
    </div>
  `
})
export class ToastComponent {
  private readonly toastService: ToastService;
  toasts$;

  constructor(toastService: ToastService) {
    this.toastService = toastService;
    this.toasts$ = this.toastService.getToasts();
  }

  getAlertClass(type: string): string {
    switch (type) {
      case 'success':
        return 'alert-success';
      case 'error':
        return 'alert-error';
      case 'warning':
        return 'alert-warning';
      case 'info':
        return 'alert-info';
      default:
        return '';
    }
  }
}
