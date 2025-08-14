import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {AuthService} from '../service/auth.service';
import {environment} from '../../../../environments/environment';
import {RegisterForm} from '../auth-model';
import {ToastService} from '../../shared/toast/toast.service';

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password')?.value;
  const confirm = control.get('passwordConfirm')?.value;
  return password && confirm && password !== confirm ? {passwordMismatch: true} : null;
};

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  error: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.registerForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(80)]],
      passwordConfirm: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(80)]]
    }, {validators: passwordMatchValidator});
  }

  get passwordsMatch(): boolean {
    return this.registerForm.get('password')?.value === this.registerForm.get('passwordConfirm')?.value;
  }


  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    if (this.registerForm.value.password !== this.registerForm.value.passwordConfirm) {
      this.error = 'Passwords do not match';
      this.loading = false;
      return;
    }

    const form: RegisterForm = {
      email: this.registerForm.value.email,
      username: this.registerForm.value.username,
      password: this.registerForm.value.password
    }

    this.authService.register(form).subscribe({
      next: (response) => {
        if (response.success && response.status === 200) {
          localStorage.setItem("token", response.data.token);
          localStorage.setItem("refresh_token", response.data.refresh_token);
          this.loading = false;
          this.router.navigate(['/explore']).then(() => {
            this.toastService.success("Registration successful");
          })
        } else {
          this.toastService.error('Registration failed')
          this.loading = false;
        }
      },
      error: error => {
        console.error('Registration error:', error);
        if (error.status === 403) {
          this.error = error.error.text || 'Registration failed';
          this.loading = false;
        } else {
          this.error = error.error.message || 'Registration failed';
          this.loading = false;
        }
      }
    });
  }

  registerWithGoogle(): void {
    window.location.href = environment.apiUrl + '/oauth2/authorization/google';
  }

  getCurrentYear() {
    return new Date().getFullYear();
  }
}
