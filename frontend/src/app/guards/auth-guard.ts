import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

/**
 * Protects routes requiring authentication. Redirects to /login using UrlTree if no valid token exists.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Returning UrlTree is the modern Angular practice to prevent canceled navigation blank screens
  return router.createUrlTree(['/login']);
};
