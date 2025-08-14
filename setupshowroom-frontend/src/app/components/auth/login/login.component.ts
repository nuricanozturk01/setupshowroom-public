import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {AuthService} from '../service/auth.service';
import {LoginForm} from '../auth-model';
import {environment} from '../../../../environments/environment';
import {ToastService} from '../../shared/toast/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  error: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(80)]]
    });
  }

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    const refreshToken = this.route.snapshot.queryParamMap.get('refresh_token');

    if (token && refreshToken) {
      this.authService.loginOauth(token, refreshToken);
      this.router.navigate(['/feed']).then(() => {
        this.toastService.success('Login Successfully');
      });
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const loginForm: LoginForm = {
      username_or_email: this.loginForm.value.email,
      password: this.loginForm.value.password
    }

    this.authService.login(loginForm).subscribe({
      next: (response) => {
        if (response.success && response.status === 200) {
          this.authService.setTokens(response.data.token, response.data.refresh_token);
          this.loading = false;
          this.router.navigate(['/feed']).then(() => {
            this.toastService.success("Login successful");
          })
        } else {
          this.toastService.warning(response.message)
          this.loading = false;
        }
      },
      error: error => {
        switch (error.status) {
          case 401:
            this.toastService.warning(error.error.text);
            this.loading = false;
            break;
          default:
            this.toastService.warning(error.message || 'Login failed');
            this.loading = false;
        }
      }
    });
  }

  loginWithGoogle(): void {
    window.location.href = environment.apiUrl + '/oauth2/authorization/google';
  }

  getCurrentYear() {
    return new Date().getFullYear();
  }
}
