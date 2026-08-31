import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  registerForm: FormGroup;
  errorMessage = '';
  successMessage = '';
  isSubmitting = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['VICTIM', [Validators.required]],
    });
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService.register(this.registerForm.value).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.successMessage = 'Account created successfully! Redirecting...';
        setTimeout(() => this.redirectToRoleDashboard(res.role), 800);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
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
