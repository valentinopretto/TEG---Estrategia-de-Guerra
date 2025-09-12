import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule, NgClass } from '@angular/common';
import { Router } from '@angular/router';
import { NotificationService } from '../../../../core/services/notification.service';
import { UserProfileDto, UserService } from '../../services/user.service';
import { ChangePasswordModalComponent } from '../change-password/change-password-modal.component';

interface GameHistory {
  fecha: string;
  rivales: string;
  resultado: string;
}

interface UserData {
  username: string;
  email: string;
}

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  imports: [
    CommonModule,
    FormsModule,
    NgClass,
    ChangePasswordModalComponent
  ],
  styleUrls: ['./user-profile.component.css'],
})
export class UserProfileComponent {

  userId?: number;

  isChangePasswordModalVisible = false;

  ngOnInit(): void {
    const storedId = localStorage.getItem('teg_current_user');

    if (!storedId) {
      this.notificationService.showNotification('error', 'Error', 'No se encontró el ID del usuario.');
      return;
    }

    try {
      const userObject = JSON.parse(storedId);

      if (!userObject.id) {
        console.error('El objeto de usuario no contiene ID.');
        return;
      }

      this.userId = userObject.id;
      if(this.userId){
          this.userService.getUserById(this.userId).subscribe((user: UserProfileDto) => {
                  this.userData = {
                    username: user.username,
                    email: user.email
                  };
                });
      }

    } catch (error) {
      this.notificationService.showNotification('error', 'Error', 'Error al obtener los datos del usuario.');
    }
  }

  userData: UserData = {
    username: '',
    email: ''
  };

  gameHistory: GameHistory[] = [
    { fecha: '06/06/2025', rivales: 'Carlos, Ana', resultado: 'Victoria' },
    { fecha: '05/06/2025', rivales: 'Miguel', resultado: 'Derrota' },
    { fecha: '04/06/2025', rivales: 'Laura, Pedro', resultado: 'Victoria' },
    { fecha: '03/06/2025', rivales: 'Sofia', resultado: 'Empate' }
  ];

  constructor(
    private notificationService: NotificationService,
    private userService: UserService,
    private router: Router
  ) {}

  editingFields = {
    username: false,
    email: false
  };

  toggleEdit(field: keyof typeof this.editingFields) {
    this.editingFields[field] = !this.editingFields[field];
  }

  saveField(field: keyof typeof this.editingFields) {
    this.editingFields[field] = false;
    console.log(`Campo ${field} guardado:`, this.userData[field]);
  }

  isSaving = false;

  saveAllChanges() {
    this.isSaving = true;
    if(this.userId){
       this.userService.updateUser(this.userId, this.userData).subscribe({
            next: () => {
              this.notificationService.showNotification('success', 'Perfil actualizado', 'Los cambios fueron guardados exitosamente.');
              Object.keys(this.editingFields).forEach(key => {
                this.editingFields[key as keyof typeof this.editingFields] = false;
              });
              this.isSaving = false;
            },
            error: () => {
              this.notificationService.showNotification('error', 'Error', 'No se pudieron guardar los cambios.');
              this.isSaving = false;
            }
          });
    }

  }

  changePassword() {
    this.isChangePasswordModalVisible = true;
  }

  closeChangePasswordModal() {
    this.isChangePasswordModalVisible = false;
  }

  editAvatar() {
    this.notificationService.showNotification('warning', 'ATENCION.', 'Funcionalidad de cambiar ávatar sin implementar.')
  }

  closeProfile() {
    this.router.navigate(['/lobby']);
  }

  getResultClass(resultado: any): string {
    if (!resultado) return '';

    const resultadoLower = resultado.toString().toLowerCase().trim();

    switch(resultadoLower) {
      case 'victoria':
      case 'ganó':
      case 'win':
      case 'ganada':
        return 'text-green-400';

      case 'derrota':
      case 'perdió':
      case 'lose':
      case 'perdida':
        return 'text-red-400';

      case 'empate':
      case 'tie':
      case 'draw':
      case 'empató':
        return 'text-yellow-400';

      default:
        return '';
    }
  }

  trackByFn(index: number, item: GameHistory): string {
    return item.fecha + item.rivales;
  }
}
