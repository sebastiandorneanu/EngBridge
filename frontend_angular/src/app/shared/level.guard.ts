import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';

export const levelGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const toastService = inject(ToastService);
  const router = inject(Router);

  // 1. Determine required level
  let requiredLevel = route.data?.['requiredLevel'];
  
  // If not in data, check for levelId in params (for generic routes like courses-learn/:levelId)
  if (requiredLevel === undefined && route.params?.['levelId']) {
    requiredLevel = parseInt(route.params['levelId'], 10);
  }

  // B1 (Level 1) is always accessible if logged in
  if (!requiredLevel || requiredLevel <= 1) {
    return true;
  }

  // 2. Check user's current level
  const userLevel = authService.getCurrentLevel();

  if (userLevel >= requiredLevel) {
    return true;
  }

  // 3. Block and Notify with specific Romanian message
  const levelName = requiredLevel === 2 ? 'B2' : 'C1';
  toastService.warning(`Nu ai deblocat inca ${levelName}!`);
  
  router.navigate(['/']);
  return false;
};