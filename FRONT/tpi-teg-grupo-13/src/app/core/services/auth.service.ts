// src/app/core/services/auth.service.ts (SUPER SIMPLE)
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// DTOs básicos
export interface UserLoginDto {
  identity: {
    type: 'USERNAME';
    username: string;
  }
  password: string;
}

export interface UserRegisterDto {
  username: string;
  email: string;
  password: string;
}

// export interface AuthResponseDto {
//   success: boolean;
//   user: any;
//   sessionId: string;
// }

export interface SimpleUser {
  id: number;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly USER_KEY = 'teg_current_user';

  constructor(private http: HttpClient) {}

  /**
   * 🔐 Login - solo llama al backend y guarda el usuario
   */
  login(credentials: UserLoginDto): Observable<SimpleUser> {
    return this.http.post<SimpleUser>(`${this.API_URL}/login`, credentials);
  }

  /**
   * 📝 Register - solo llama al backend y guarda el usuario
   */
  register(userData: UserRegisterDto): Observable<SimpleUser> {
    return this.http.post<SimpleUser>(`${this.API_URL}/register`, userData);
  }

  /**
   * 💾 Guardar usuario en localStorage
   */
  saveUser(user: any): void {
    const simpleUser: SimpleUser = {
      id: user.id,
      username: user.username,
      email: user.email
    };

    localStorage.setItem(this.USER_KEY, JSON.stringify(simpleUser));
    console.log('💾 Usuario guardado:', simpleUser);
  }

  /**
   * 👤 Obtener usuario actual
   */
  getCurrentUser(): SimpleUser | null {
    try {
      const userData = localStorage.getItem(this.USER_KEY);
      if (!userData) return null;

      return JSON.parse(userData);
    } catch (error) {
      console.error('Error obteniendo usuario:', error);
      return null;
    }
  }

  /**
   * ✅ Verificar si está autenticado (SUPER SIMPLE)
   */
  isAuthenticated(): boolean {
    const user = this.getCurrentUser();
    return user !== null;
  }

  /**
   * 🚪 Cerrar sesión
   */
  logout(): void {
    localStorage.removeItem(this.USER_KEY);
    console.log('🚪 Usuario deslogueado');
  }
}
