import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * ======================================================================================
 * HTTP INTERCEPTOR FUNCIONAL (Angular 18 Standalone)
 * ======================================================================================
 * Inyecta las credenciales y roles del usuario activo en todas las peticiones salientes
 * hacia el backend Spring Boot:
 * - Authorization: Bearer <token>
 * - X-User-Role: ROLE_ADMIN | ROLE_OPERATOR
 * ======================================================================================
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const currentUser = authService.getCurrentUser();

  const cloned = req.clone({
    setHeaders: {
      'Authorization': `Bearer ${currentUser.token}`,
      'X-User-Role': currentUser.role
    }
  });

  return next(cloned);
};
