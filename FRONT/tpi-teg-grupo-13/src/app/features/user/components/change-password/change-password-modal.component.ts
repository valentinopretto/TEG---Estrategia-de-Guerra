import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService, ChangePasswordDto } from '../../services/user.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-change-password-modal',
  templateUrl: './change-password-modal.component.html',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  styleUrls: ['./change-password-modal.component.css']
})

export class ChangePasswordModalComponent {
  @Input() isVisible: boolean = false;
  @Output() onClose = new EventEmitter<void>();

  passwordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  isChanging = false;

  constructor(
    private userService: UserService,
    private notificationService: NotificationService,
  ) {}

  closeModal() {
    this.resetForm();
    this.onClose.emit();
  }

  resetForm() {
    this.passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    this.isChanging = false;
  }

  onSubmit() {
    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      this.notificationService.showNotification('error', 'Error', 'Las contraseñas no coinciden.');
      return;
    }

    if (this.passwordData.newPassword.length < 6) {
      this.notificationService.showNotification('error', 'Error', 'La nueva contraseña debe tener al menos 6 caracteres.');
      return;
    }

    this.isChanging = true;

    // Obtener el ID del usuario del localStorage
    const storedId = localStorage.getItem('teg_current_user');
    if (!storedId) {
      this.notificationService.showNotification('error', 'Error', 'No se encontró el ID del usuario.');
      this.isChanging = false;
      return;
    }

    try {
      const userObject = JSON.parse(storedId);
      const userId = userObject.id?.toString();

      if (!userId) {
        this.notificationService.showNotification('error', 'Error', 'ID de usuario inválido.');
        this.isChanging = false;
        return;
      }

      const changePasswordData: ChangePasswordDto = {
        currentPassword: this.passwordData.currentPassword,
        newPassword: this.passwordData.newPassword
      };

      this.userService.changePassword(userId, changePasswordData).subscribe({
        next: () => {
          this.notificationService.showNotification('success', 'Éxito', 'Contraseña modificada exitosamente.');
          this.closeModal();
        },
        error: (error: any) => {
          console.error('Error changing password:', error);
          const errorMessage = error.error?.message || 'Error al modificar la contraseña. Verifica que la contraseña actual sea correcta.';
          this.notificationService.showNotification('error', 'Error', errorMessage);
          this.isChanging = false;
        }
      });


    } catch (error) {
      this.notificationService.showNotification('error', 'Error', 'Error al procesar los datos del usuario.');
      this.isChanging = false;
    }
  }
}
