import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  loginForm: FormGroup;
  errorMessage = '';
  isSubmitting = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.redirectToRoleDashboard(res.role);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Invalid username or password.';
      },
    });
  }

  private redirectToRoleDashboard(role: string): void {
    if (role === 'OFFICER') {
      this.router.navigate(['/officer/dashboard']);
    } else if (role === 'VOLUNTEER') {
      this.router.navigate(['/volunteer/dashboard']);
    } else if (role === 'VICTIM') {
      this.router.navigate(['/victim/my-sos']);
    } else {
      this.router.navigate(['/dashboard']);
    }
  }
}
