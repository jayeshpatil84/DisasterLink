import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth';

/**
 * Protects routes requiring specific roles.
 * Usage: canActivate: [roleGuard], data: { expectedRole: 'OFFICER' } or data: { expectedRoles: ['VOLUNTEER', 'OFFICER'] }
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  const currentRole = authService.getRole();
  const expectedRole = route.data['expectedRole'] as string | undefined;
  const expectedRoles = route.data['expectedRoles'] as string[] | undefined;

  if (expectedRole && currentRole === expectedRole) {
    return true;
  }

  if (expectedRoles && currentRole && expectedRoles.includes(currentRole)) {
    return true;
  }

  // Redirect to appropriate dashboard or home based on role
  if (currentRole === 'OFFICER') {
    return router.createUrlTree(['/officer/dashboard']);
  } else if (currentRole === 'VOLUNTEER') {
    return router.createUrlTree(['/volunteer/dashboard']);
  } else if (currentRole === 'VICTIM') {
    return router.createUrlTree(['/victim/my-sos']);
  }

  return router.createUrlTree(['/login']);
};
