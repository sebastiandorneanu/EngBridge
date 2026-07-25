import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const toastService = inject(ToastService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  toastService.info('Please login to access this content.');
  router.navigate(['/login']);
  return false;
};