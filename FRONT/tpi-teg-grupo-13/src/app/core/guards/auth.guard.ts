// src/app/core/guards/auth.guard.ts (SUPER SIMPLE)
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * 🔒 Guard para rutas protegidas
 * Solo verifica si hay usuario en localStorage
 */
export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  const isAuth = authService.isAuthenticated();
  
  if (isAuth) {
    console.log('✅ Usuario autenticado, acceso permitido');
    return true;
  } else {
    console.log('❌ No hay usuario, redirigiendo a login');
    router.navigate(['/login']);
    return false;
  }
};

/**
 * 🔓 Guard para rutas públicas (login/register)
 * Si ya hay usuario, redirige al lobby
 */
export const guestGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  const isAuth = authService.isAuthenticated();
  
  if (isAuth) {
    console.log('✅ Usuario ya logueado, redirigiendo a lobby');
    router.navigate(['/lobby']);
    return false;
  } else {
    console.log('🔓 No hay usuario, acceso permitido a página pública');
    return true;
  }
};