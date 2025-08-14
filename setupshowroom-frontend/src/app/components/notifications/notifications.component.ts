import { Component, HostListener, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Notification } from './model/notification.model';
import { NotificationService } from './service/notification.service';
import {formatDate, gravatarUrl} from '../../utils/util';
import { SetupPostComponent } from '../setup/setup-post/setup-post.component';
import { SetupInfo } from '../setup/setup-model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule, NgOptimizedImage, SetupPostComponent],
  templateUrl: './notifications.component.html',
})
export class NotificationsComponent implements OnInit {
  @ViewChild('setupModal') setupModal!: ElementRef<HTMLDialogElement>;
  selectedSetup: SetupInfo | null = null;

  public notifications: Notification[] = [];
  public loading = true;
  public showOnlyUnread = true;
  public currentPage = 0;
  public isLastPage = false;
  public loadingMore = false;
  private readonly pageSize = 20;

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.loadNotifications();
    this.notificationService.getNotifications$().subscribe(n => {
      this.notifications = n;
      this.loading = false;
    });
  }
  isClickable(type: string): boolean {
    const clickableTypes = [
      'LIKE',
      'FAVORITE',
      'COMMENT',
      'REPLY',
      'MENTION',
      'FOLLOW',
      'REPOST',
      'TAG'
    ];
    return clickableTypes.includes(type);
  }

  @HostListener('window:scroll', [])
  onScroll(): void {
    if (this.loadingMore || this.isLastPage) return;

    const scrollPosition = window.pageYOffset || document.documentElement.scrollTop;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;

    if (documentHeight - (scrollPosition + windowHeight) < 100) {
      this.loadMoreNotifications();
    }
  }

  loadNotifications() {
    this.notificationService.loadNotifications(0, this.pageSize).subscribe(last => this.isLastPage = last);
    this.currentPage = 0;
  }

  loadMoreNotifications() {
    if (this.loadingMore || this.isLastPage) return;
    this.loadingMore = true;
    this.currentPage++;
    this.notificationService.loadNotifications(this.currentPage, this.pageSize, true).subscribe(last => {
      this.isLastPage = last;
      this.loadingMore = false;
    });
  }

  get filteredNotifications(): Notification[] {
    return this.showOnlyUnread ? this.notifications.filter(n => !n.read) : this.notifications;
  }

  get hasUnreadNotifications(): boolean {
    return this.notifications.some(n => !n.read);
  }

  markAsRead(notification: Notification) {
    this.notificationService.markAsRead(notification.id);
  }

  markAllAsRead() {
    this.notificationService.markAllAsRead();
  }

  openSetupModal(setup: SetupInfo) {
    this.selectedSetup = setup;
    if (this.setupModal?.nativeElement) {
      this.setupModal.nativeElement.showModal();
    }
  }

  closeSetupModal() {
    this.selectedSetup = null;
    if (this.setupModal?.nativeElement) {
      this.setupModal.nativeElement.close();
    }
  }

  protected readonly gravatarUrl = gravatarUrl;
  protected readonly formatDate = formatDate;
}
