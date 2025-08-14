import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {AuthService} from '../auth/service/auth.service';
import {NotificationService} from '../notifications/service/notification.service';
import {gravatarUrl} from '../../utils/util';
import {UserInfo} from '../profile/model/profile.model';
import {ToastService} from '../shared/toast/toast.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {
  public userInfo!: UserInfo;
  public unreadCount = 0;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService,
    private notificationService: NotificationService) {
  }

  ngOnInit() {
    this.authService.getUserInfo().subscribe(response => {
      if (response.success && response.status === 200) {
        this.userInfo = response.data;
      }
    })

    this.notificationService.getUnreadCount().subscribe(count => {
      this.unreadCount = count;
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']).then(() => {
      this.toastService.success('Logged out successfully!');
    })
  }

  protected readonly gravatarUrl = gravatarUrl;
}
