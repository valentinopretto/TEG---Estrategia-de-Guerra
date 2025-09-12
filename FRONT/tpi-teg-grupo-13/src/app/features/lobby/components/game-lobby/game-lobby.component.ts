import {Component, OnInit, OnDestroy} from '@angular/core';
import {GameJoinDto, GameResponseDto, LobbyService} from '../../services/lobby.service';
import {CommonModule} from '@angular/common';
import {GameCreationDto} from '../../services/lobby.service';
import {AuthService} from '../../../../core/services/auth.service';
import Swal from 'sweetalert2';
import {ActivatedRoute, Router} from '@angular/router';
import {NotificationService} from '../../../../core/services/notification.service';
import {firstValueFrom, Subscription} from 'rxjs';

@Component({
  selector: 'app-game-lobby',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './game-lobby.component.html',
  styleUrl: './game-lobby.component.css'
})
export class GameLobbyComponent implements OnInit, OnDestroy {

  userId: string = '';

  currentUser: any = null;
  isLoading = false;
  private subscriptions: Subscription[] = [];

  gameCodeCreated: string = ''; // Variable para guardar el código de la partida que se puede llegar a crear
  // se usará para mandarlo al otro componente(create-game-modal) y cargar el juego allá

  constructor(
    private lobbyService: LobbyService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    if (!this.currentUser || !this.currentUser.id) {
      this.notificationService.showNotification('error', 'Error', 'No se encontró el usuario actual.');
      return;
    }

    this.userId = this.currentUser.id.toString();
    //this.loadAvailableGames();
    console.log(this.userId);
  }


  ngOnDestroy(): void {
    // Limpiar subscripciones para evitar memory leaks
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  /**
   * Carga los juegos disponibles para mostrar en el lobby
   */


  /**
   * Crea una nueva partida y redirige al usuario a la pantalla de configuración de la partida
   * Mandándole el gameCode creado para que se pueda cargar el juego en la pantalla de configuración
   */


  async createGameAndRedirect(): Promise<void> {
    // validar que haya un usuario
    if (!this.currentUser || !this.currentUser.id) {
      console.error('Debes estar logueado para crear una partida');
      Swal.fire({
        title: 'Error',
        text: 'Debes estar logueado para crear una partida',
        icon: 'error'
      });
      return;
    }
    console.log(this.userId);
    try {
      Swal.fire({
        title: 'Creando partida...',
        text: 'Por favor espera',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      const gameResponse = await firstValueFrom(
        this.lobbyService.createGame(this.currentUser.id)
      );

      Swal.close();

      if (gameResponse?.gameCode) {
        this.gameCodeCreated = gameResponse.gameCode;
        await this.router.navigate(['/create-game', gameResponse.gameCode]);

      } else {
        throw new Error('No se recibió un código de juego válido');
      }


    } catch (error: any) {
      Swal.close();
      console.error('Error al crear la partida:', error);
      Swal.fire({
        title: 'Error',
        text: error.message || 'No se pudo crear la partida. Intenta nuevamente.',
        icon: 'error'
      });
    }
  }



  /**
   * Ir a configuración de usuario
   */
  goToUserSettings(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      console.error('Debes estar logueado para acceder a la configuración');
      Swal.fire({
        title: 'Error',
        text: 'Debes estar logueado para acceder a la configuración',
        icon: 'error'
      });
      return;
    }

    this.router.navigate(['/settings']);
  }


  /**
   * Cerrar sesión
   */
  logout(): void {
    Swal.fire({
      title: '¿Estás seguro?',
      text: 'Se cerrará tu sesión actual',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, cerrar sesión',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.authService.logout();
        this.router.navigate(['/login']);
      }
    });
  }

  /**
   * Verificar si el usuario puede crear una partida
   */
  canCreateGame(): boolean {
    return this.currentUser && !this.isLoading;
  }

  /**
   * Verificar si el usuario puede unirse a juegos
   */
  canJoinGames(): boolean {
    return this.currentUser && !this.isLoading;
  }

  joinGameAndRedirect() {
    this.router.navigate(['/join-game']);
    
  }
}
