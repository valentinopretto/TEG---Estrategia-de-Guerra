import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import Swal from 'sweetalert2';

import {
  LobbyService,
  AddBotsDto,
  UpdateGameSettingsDto,
  GameResponseDto
} from '../../services/lobby.service';
import { AuthService } from '../../../../core/services/auth.service';
import { BotLevel } from '../../../../core/enums/BotLevel';
import { BotStrategy } from '../../../../core/enums/BotStrategy';
import { GamePlayer } from '../../../../core/models/interfaces/game-player';

@Component({
  selector: 'app-game-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './create-game.component.html',
  styleUrls: ['create-game.component.css']
})
export class GameCreateComponent implements OnInit, OnDestroy {
  gameCode = '';
  userId = 0;
  hostUsername = '';
  isJoinMode: boolean = false;

  // Configuraciones iniciales
  maxPlayers = 5;
  chatEnabled = false;
  turnTime = 60;

  botLevel!: BotLevel;
  botLevels = Object.values(BotLevel);
  botStrategy!: BotStrategy;
  botStrategies = Object.values(BotStrategy);

  players: GamePlayer[] = [];
  private pollingSub?: Subscription;
  private updateTimeout: any;

  constructor(
    private lobbyService: LobbyService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('gameCode');
    if (!code) {
      Swal.fire('Error', 'Código de juego inválido', 'error');
      this.router.navigate(['/lobby']);
      return;
    }
    this.gameCode = code;
    localStorage.setItem("gameCodeAuxiliary", this.gameCode);

    this.isJoinMode = this.route.snapshot.queryParams['mode'] === 'join';

    this.userId = this.authService.getCurrentUser()?.id ?? 0;
    if (this.userId === 0) {
      Swal.fire({
        icon: 'error',
        title: 'Error de autenticación',
        text: 'Debes estar autenticado para acceder a esta página.'
      });
      this.router.navigate(['/login']);
      return;
    }
    this.userId = this.authService.getCurrentUser()?.id ?? 0;

    this.botLevel = this.botLevels[0];
    this.botStrategy = this.botStrategies[0];

    this.loadGameData();
    this.startPolling();
  }

  ngOnDestroy(): void {
    if (this.updateTimeout) clearTimeout(this.updateTimeout);
    this.pollingSub?.unsubscribe();
  }

  private loadGameData(): void {
    this.lobbyService.getGameByCode(this.gameCode).subscribe({
      next: game => {
        this.players = game.players;
        this.maxPlayers = game.maxPlayers;
        this.turnTime = game.turnTimeLimit;
        this.chatEnabled = game.chatEnabled;
        this.hostUsername = game.createdByUsername;
      },
      error: () => {
        Swal.fire('Error', 'No se pudo cargar la partida', 'error');
        this.router.navigate(['/lobby']);
      }
    });
  }

  private startPolling(): void {
    this.pollingSub = interval(5000).subscribe(() => {
      this.lobbyService.getGameByCode(this.gameCode).subscribe({
        next: (game: GameResponseDto) => {
          console.log('Actualización de jugadores:', game.players);
          this.players = game.players || [];
        },
        error: (err) => {
          console.error('Error al actualizar lista de jugadores:', err);
        }
      });
    });
  }

  getEmptySlots(): number[] {
    const empty = this.maxPlayers - this.players.length;
    return Array.from({ length: Math.max(0, empty) }, (_, i) => i);
  }

  // Método para obtener el nombre del jugador manejando diferentes estructuras de datos
  getPlayerName(player: any): string {
    console.log('Procesando jugador:', player);

    // Si tiene la estructura GamePlayer con user
    if (player.user && player.user.name) {
      return player.user.name;
    }

    // Si tiene la estructura GamePlayer con user pero sin name
    if (player.user && player.user.username) {
      return player.user.username;
    }

    // Si tiene name directamente
    if (player.name) {
      return player.name;
    }

    // Si tiene username directamente
    if (player.username) {
      return player.username;
    }

    // Si tiene email (como fallback)
    if (player.email) {
      return player.email.split('@')[0]; // Solo la parte antes del @
    }

    // Si es un bot
    if (player.isBot || player.botLevel) {
      return `Bot ${player.botLevel || 'NOVICE'}`;
    }

    // Fallback
    return 'Jugador desconocido';
  }

  completeGameCreation(): void {
    if (this.isJoinMode) {
      // En modo join, solo navegar al juego sin modificar configuraciones
      this.router.navigate([`/game/${this.gameCode}`]);
      return;
    }

    const updateGameSettingsDto: UpdateGameSettingsDto = {
      requesterId: this.userId,
      maxPlayers: this.maxPlayers,
      turnTimeLimit: this.turnTime,
      chatEnabled: this.chatEnabled,
      pactsAllowed: false
    };

    this.lobbyService.updateGameSettings(this.gameCode, updateGameSettingsDto).subscribe({
      next: game => {
        this.players = game.players;
        const toAdd = this.maxPlayers - this.players.length;

        if (toAdd > 0) {
          // Añadir bots si faltan jugadores
          this.addBotsAndStartGame(toAdd);
        } else {
          // Iniciar el juego directamente
          this.startGame();
        }
      },
      error: err => Swal.fire('Error', err.message, 'error')
    });
  }

  // Método para añadir un bot manualmente (sin iniciar juego)
  addSingleBot(): void {
    const dto: AddBotsDto = {
      gameCode: this.gameCode,
      numberOfBots: 1,
      botLevel: this.botLevel,
      botStrategy: this.botStrategy,
      requesterId: this.userId
    };

    this.lobbyService.addBots(dto).subscribe({
      next: game => {
        this.players = game.players;
        // Solo actualiza la lista, no inicia el juego
      },
      error: err => Swal.fire('Error al añadir bot', err.message, 'error')
    });
  }

  // Método para añadir bots e iniciar juego (solo en flujo de inicio)
  private addBotsAndStartGame(count: number): void {
    const dto: AddBotsDto = {
      gameCode: this.gameCode,
      numberOfBots: count,
      botLevel: this.botLevel,
      botStrategy: this.botStrategy,
      requesterId: this.userId
    };

    this.lobbyService.addBots(dto).subscribe({
      next: game => {
        this.players = game.players;
        this.startGame(); // Iniciar juego después de añadir bots
      },
      error: err => Swal.fire('Error al añadir bots', err.message, 'error')
    });
  }

  private startGame(): void {
    this.lobbyService.startGame(this.gameCode, this.userId).subscribe({
      next: (game) => {
        this.router.navigate([`/game/${this.gameCode}`]);
      },
      error: err => Swal.fire('Error al iniciar el juego', err.message, 'error')
    });
  }




  private updateGameSettings(): void {
    // Cancelar actualización pendiente si existe
    if (this.updateTimeout) {
      clearTimeout(this.updateTimeout);
    }

    // Programar nueva actualización con debounce
    this.updateTimeout = setTimeout(() => {
      const dto: UpdateGameSettingsDto = {
        requesterId: this.userId,
        maxPlayers: this.maxPlayers,
        turnTimeLimit: this.turnTime,
        chatEnabled: this.chatEnabled,
        pactsAllowed: false
      };

      this.lobbyService.updateGameSettings(this.gameCode, dto).subscribe({
        next: (game) => {
          // Actualizar solo los ajustes que podrían cambiar desde el servidor
          this.maxPlayers = game.maxPlayers;
          this.turnTime = game.turnTimeLimit;
          this.chatEnabled = game.chatEnabled;

          // Actualizar jugadores (por si se eliminaron al reducir maxPlayers)
          this.players = game.players;
        },
        error: (err) => {
          Swal.fire('Error', 'No se pudieron guardar los ajustes: ' + err.message, 'error');
          // Recargar datos originales
          this.loadGameData();
        }
      });
    }, 500); // Debounce de 500ms
  }

  decreaseMaxPlayers(): void {
    if (this.maxPlayers > 2) {
      this.maxPlayers--;
      this.updateGameSettings();
    }
  }

  increaseMaxPlayers(): void {
    if (this.maxPlayers < 6) {
      this.maxPlayers++;
      this.updateGameSettings();
    }
  }

  decreaseTurnTime(): void {
    if (this.turnTime > 15) {
      this.turnTime -= 5;
      this.updateGameSettings();
    }
  }

  increaseTurnTime(): void {
    if (this.turnTime < 120) {
      this.turnTime += 5;
      this.updateGameSettings();
    }
  }

  // Actualizar cuando cambia el estado del chat
  onChatToggle(): void {
    this.updateGameSettings();
  }

  onCancel(): void {
    Swal.fire({
      title: '¿Salir y cancelar partida?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, salir'
    }).then(res => {
      if (res.isConfirmed) {
        this.pollingSub?.unsubscribe();
        this.lobbyService.cancelGame(this.gameCode, this.hostUsername)
          .subscribe(() => this.router.navigate(['/lobby']));
      }
    });
  }

  protected readonly BotStrategy = BotStrategy;
}
