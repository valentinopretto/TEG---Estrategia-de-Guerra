// src/app/features/join-game/components/join-game.component.ts
import {FormsModule} from '@angular/forms';
import {Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import {CommonModule} from '@angular/common';
import { GameService, SavedGame, UnirseResponse, ContinuarResponse } from '../services/game.service';
import {GameLobbyComponent} from '../../lobby/components/game-lobby/game-lobby.component';
import Swal from 'sweetalert2';
import {firstValueFrom} from 'rxjs';
import {LobbyService} from '../../lobby/services/lobby.service';

@Component({
  selector: 'app-join-game',
  templateUrl: './join-game.component.html',
  styleUrls: ['./join-game.component.css'],
  providers: [GameService, LobbyService],
  imports: [FormsModule, CommonModule],
})
export class JoinGameComponent implements OnInit {
  partidas: SavedGame[] = [];
  codigo: string = '';
  loading: boolean = false;
  error: string = '';

  constructor(
    private gameService: GameService,
    private router: Router,
    private lobbyService: LobbyService
  ) {}

  ngOnInit(): void {
    this.cargarPartidasGuardadas();
  }

  cargarPartidasGuardadas(): void {
    this.loading = true;
    this.error = '';
    this.gameService.obtenerPartidasDelHostAdaptadas().subscribe({
      next: (partidas) => {
        this.partidas = partidas;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar las partidas';
        this.loading = false;
      }
    });
  }

  backToLobby(): void {
    this.router.navigate(['/lobby']);
  }

  unirseConCodigo(): void {
    if (!this.codigo.trim()) {
      this.error = 'Ingresa un código válido';
      return;
    }

    this.loading = true;
    this.error = '';

    this.gameService.unirseConCodigo(this.codigo.trim()).subscribe({
      next: _ => {
        this.loading = false;
        this.router.navigate(['/create-game', this.codigo.trim()], {
          queryParams: { mode: 'join' }
        });
      },
      error: _ => {
        this.error = 'Error al unirse';
        this.loading = false;
      }
    });
  }

  continuarPartida(id: string): void {
    this.loading = true;
    this.error = '';

    this.gameService.continuarPartida(id).subscribe({
      next: res => {
        this.loading = false;
        this.router.navigate(['/game', res.partidaId]);
      },
      error: _ => {
        this.error = 'Error al continuar';
        this.loading = false;
      }
    });
  }

  eliminarPartida(id: string): void {
    if (!confirm('¿Estás seguro de que quieres eliminar esta partida?')) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.gameService.eliminarPartida(id).subscribe({
      next: _ => {
        this.partidas = this.partidas.filter(p => p.id !== id);
        this.loading = false;
      },
      error: _ => {
        this.error = 'Error al eliminar';
        this.loading = false;
      }
    });
  }

  async crearNuevaPartida(): Promise<void> {
    try {
      // Obtener userId desde localStorage
      const userId = this.getUserIdFromStorage();
      console.log('userId obtenido:', userId);

      if (!userId) {
        console.error('Debes estar logueado para crear una partida');
        Swal.fire({
          title: 'Error',
          text: 'Debes estar logueado para crear una partida',
          icon: 'error'
        });
        return;
      }

      // Mostrar loading
      Swal.fire({
        title: 'Creando partida...',
        text: 'Por favor espera',
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      // Crear la partida
      const gameResponse = await firstValueFrom(
        this.lobbyService.createGame(userId)
      );

      // Cerrar loading
      Swal.close();

      // Validar respuesta y navegar
      if (gameResponse?.gameCode) {
        this.codigo = gameResponse.gameCode;
        await this.router.navigate(['/create-game', gameResponse.gameCode]);
      } else {
        throw new Error('No se recibió un código de juego válido');
      }

    } catch (error: any) {
      // Cerrar loading y mostrar error
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
   * Obtiene el userId desde localStorage
   * Busca específicamente en teg_current_user y otras keys comunes
   */
  private getUserIdFromStorage(): number | null {
    try {
      // Buscar primero en teg_current_user (tu app específica)
      const tegCurrentUser = localStorage.getItem('teg_current_user');
      if (tegCurrentUser) {
        try {
          const user = JSON.parse(tegCurrentUser);
          if (user && user.id) {
            const numericUserId = Number(user.id);
            if (!isNaN(numericUserId) && numericUserId > 0) {
              return numericUserId;
            }
          }
        } catch (parseError) {
          console.warn('Error parsing teg_current_user from localStorage:', parseError);
        }
      }

      // Intentar keys directas de ID
      const possibleKeys = ['userId', 'user_id', 'id', 'currentUserId'];

      for (const key of possibleKeys) {
        const storedValue = localStorage.getItem(key);
        if (storedValue) {
          const numericValue = Number(storedValue);
          if (!isNaN(numericValue) && numericValue > 0) {
            return numericValue;
          }
        }
      }

      // Intentar obtener desde otros objetos user almacenados
      const otherUserKeys = ['user', 'currentUser', 'current_user'];
      for (const key of otherUserKeys) {
        const userString = localStorage.getItem(key);
        if (userString) {
          try {
            const user = JSON.parse(userString);
            if (user && (user.id || user.userId)) {
              const userId = user.id || user.userId;
              const numericUserId = Number(userId);
              if (!isNaN(numericUserId) && numericUserId > 0) {
                return numericUserId;
              }
            }
          } catch (parseError) {
            console.warn(`Error parsing ${key} object from localStorage:`, parseError);
          }
        }
      }

      return null;
    } catch (error) {
      console.error('Error accessing localStorage:', error);
      return null;
    }
  }

  /**
   * Función auxiliar para verificar si el usuario está logueado
   */
  private isUserLoggedIn(): boolean {
    return this.getUserIdFromStorage() !== null;
  }

  trackByPartidaId(_idx: number, item: SavedGame): string {
    return item.id;
  }

  formatearFecha(fecha: Date | string): string {
    try {
      const d = typeof fecha === 'string' ? new Date(fecha) : fecha;
      return d.toLocaleString('es-ES');
    } catch (error) {
      console.error('Error formatting date:', error);
      return 'Fecha inválida';
    }
  }
}
