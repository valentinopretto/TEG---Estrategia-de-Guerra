// src/app/features/lobby/services/lobby.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, of } from 'rxjs';
import { catchError, switchMap, takeWhile } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { BotLevel } from '../../../core/enums/BotLevel';
import { BotStrategy } from '../../../core/enums/BotStrategy';

// DTOs
export interface GameCreationDto {
  hostUserId: number;
}

export interface GameJoinDto {
  userId: number;
  gameCode: string;
}
export interface StartGameDto {
  gameCode: string;
  userId: number;
}

export interface LeaveGameDto {
  userId: number;
  gameCode: string;
}

export interface GameResponseDto {
  id: number;
  gameCode: string;
  createdByUsername: string;
  status: string;
  maxPlayers: number;
  turnTimeLimit: number;
  chatEnabled: boolean;
  pactsAllowed: boolean;
  players: any[];
}

export interface AddBotsDto {
  gameCode: string;
  numberOfBots: number;
  botLevel: BotLevel;
  botStrategy: BotStrategy;
  requesterId: number;
}

export interface UpdateGameSettingsDto {
  requesterId: number;
  maxPlayers: number;
  turnTimeLimit: number;
  chatEnabled: boolean;
  pactsAllowed: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class LobbyService {
  private readonly API_URL = `${environment.apiUrl}/games`;
  private availableGamesSubject = new BehaviorSubject<GameResponseDto[]>([]);
  public availableGames$ = this.availableGamesSubject.asObservable();

  private currentGameSubject = new BehaviorSubject<GameResponseDto | null>(null);
  public currentGame$ = this.currentGameSubject.asObservable();

  private isPollingActive = false;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  // 1) Crear lobby
  createGame(hostUserId: number): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/create-lobby`,
      { hostUserId },
      { headers: this.getHeaders() }
    );
  }

  // 2) Obtener lista de partidas disponibles
  getAvailableGames(): Observable<GameResponseDto[]> {
    return this.http.get<GameResponseDto[]>(
      `${this.API_URL}/available`,
      { headers: this.getHeaders() }
    );
  }

  // 3) Obtener datos de una partida por código
  getGameByCode(gameCode: string): Observable<GameResponseDto> {
    return this.http.get<GameResponseDto>(
      `${this.API_URL}/${gameCode}`,
      { headers: this.getHeaders() }
    );
  }

  // 4) Unirse a una partida
  joinGame(dto: GameJoinDto): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/join`,
      dto,
      { headers: this.getHeaders() }
    );
  }

  // 5) Salir voluntariamente de la partida
  leaveGame(gameCode: string, userId: number): Observable<GameResponseDto> {
    const body: LeaveGameDto = { gameCode, userId };
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/leave`,
      body,
      { headers: this.getHeaders() }
    );
  }

  // 6) Cancelar partida (sólo host)
  cancelGame(gameCode: string, username: string): Observable<void> {
    const params = new HttpParams().set('username', username);
    return this.http.delete<void>(
      `${this.API_URL}/${gameCode}`,
      { params }
    );
  }

  // 7) Actualizar ajustes de la partida (sólo host)
  updateGameSettings(gameCode: string, dto: UpdateGameSettingsDto): Observable<GameResponseDto> {
    return this.http.put<GameResponseDto>(
      `${this.API_URL}/${gameCode}/settings`,
      dto,
      { headers: this.getHeaders() }
    );
  }

  // 8) Añadir bots a la partida (sólo host)
  addBots(dto: AddBotsDto): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/add-bots`,
      dto,
      { headers: this.getHeaders() }
    );
  }

  // 9) Iniciar la partida (sólo host)
  startGame(gameCode: string, userId: number): Observable<GameResponseDto> {
    const dto: StartGameDto = { gameCode, userId };

    return this.http.post<GameResponseDto>(
      `${this.API_URL}/start`,
      dto,  // Enviar el objeto con los datos requeridos
      { headers: this.getHeaders() }
    );
  }
  // Polling de partidas disponibles
  startAvailableGamesPolling(): void {
    if (this.isPollingActive) return;
    this.isPollingActive = true;
    interval(5000).pipe(
      takeWhile(() => this.isPollingActive),
      switchMap(() => this.getAvailableGames()),
      catchError(err => {
        console.error('Error polling available games', err);
        return of([]);
      })
    ).subscribe(games => this.availableGamesSubject.next(games));
  }

  // Polling de una partida en curso
  startGamePolling(gameCode: string): void {
    if (this.isPollingActive) return;
    this.isPollingActive = true;
    interval(2000).pipe(
      takeWhile(() => this.isPollingActive),
      switchMap(() => this.getGameByCode(gameCode)),
      catchError(err => {
        console.error('Error polling game', err);
        return of(null);
      })
    ).subscribe(game => {
      if (game) this.currentGameSubject.next(game);
    });
  }

  stopPolling(): void {
    this.isPollingActive = false;
  }

  clearData(): void {
    this.availableGamesSubject.next([]);
    this.currentGameSubject.next(null);
    this.stopPolling();
  }

  // Helpers
  canJoinGame(game: GameResponseDto): boolean {
    return game.status === 'WAITING_FOR_PLAYERS'
      && game.players.length < game.maxPlayers;
  }

  isGameHost(game: GameResponseDto): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.username === game.createdByUsername;
  }
}
