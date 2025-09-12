// src/app/features/auth/login/login.component.ts (COMPLETAMENTE LIMPIO)
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, UserLoginDto } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  loginData: UserLoginDto = {
    identity: {
      type: "USERNAME",
      username: ""
    },
    password: ""
  };

  isLoading = false;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/lobby']);
    }
  }

  onSubmit(form: any): void {
    if (form.valid) {
      this.isLoading = true;

      this.authService.login(this.loginData).subscribe({
        next: (user) => {
          console.log('Login response:', user);

          if (user) {
            this.authService.saveUser(user);

            this.notificationService.showNotification(
              'success',
              'Login exitoso',
              `Bienvenido ${user.username}`
            );

            this.router.navigate(['/lobby']);
          } else {
            this.notificationService.showNotification(
              'error',
              'Error',
              'Login fallido'
            );
          }

          this.isLoading = false;
        },
        error: (error) => {
          console.error('Login error:', error);
          this.isLoading = false;

          let errorMessage = 'Error desconocido';
          if (error.status === 401) {
            errorMessage = 'Usuario o contraseña incorrectos';
          } else if (error.status === 404) {
            errorMessage = 'Usuario no encontrado';
          } else if (error.status === 0) {
            errorMessage = 'No se pudo conectar con el servidor';
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }

          this.notificationService.showNotification(
            'error',
            'Error de Login',
            errorMessage
          );
        }
      });
    }
  }

  onCancel(): void {
    this.loginData = {
      identity: {
        type: "USERNAME",
        username: ""
      },
      password: ""
    };
  }

  onForgotPassword(): void {
    this.notificationService.showNotification(
      'info',
      'Recuperar Contraseña',
      'Funcionalidad próximamente disponible'
    );
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
