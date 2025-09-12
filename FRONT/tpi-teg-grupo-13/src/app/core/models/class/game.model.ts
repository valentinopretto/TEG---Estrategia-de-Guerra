// src/app/core/models/game.model.ts
import { GameState } from '../../../features/game/services/game.play.service';
import { TurnPhase } from '../../enums/turn-phase';

export interface Game {
  id: string;
  gameCode: string;
  createdBy: string;
  status: GameState;
  currentPhase?: TurnPhase;
  currentTurn: number;
  currentPlayerId?: string;
  maxPlayers: number;
  turnTimeLimit: number; // en minutos
  chatEnabled: boolean;
  pactsAllowed: boolean;
  createdAt: Date;
  startedAt?: Date;
  finishedAt?: Date;
  players: GamePlayer[];
  winnerId?: string;
}

export interface GamePlayer {
  id: string;
  userId?: string; // null si es bot
  username: string;
  isBot: boolean;
  botLevel?: string;
  color: string;
  status: 'WAITING' | 'PLAYING' | 'ELIMINATED' | 'WINNER';
  seatOrder: number;
  joinedAt: Date;
  territoriesCount: number;
  totalArmies: number;
  cardsCount: number;
  armiesToPlace: number;
}

export interface GameTerritory {
  id: string;
  countryId: string;
  countryName: string;
  ownerId?: string;
  ownerName?: string;
  armies: number;
  continentName: string;
  positionX: number;
  positionY: number;
  neighborIds: string[];
}

export interface GameOptions {
  maxPlayers: number;
  turnTimeLimit: number;
  chatEnabled: boolean;
  pactsAllowed: boolean;
}

// Clase principal del juego
export class GameModel {
  constructor(
    public id: string = '',
    public gameCode: string = '',
    public createdBy: string = '',
    public status: GameState = GameState.WAITING_FOR_PLAYERS,
    public maxPlayers: number = 6,
    public turnTimeLimit: number = 10,
    public chatEnabled: boolean = true,
    public pactsAllowed: boolean = false,
    public players: GamePlayer[] = [],
    public currentTurn: number = 0,
    public currentPhase?: TurnPhase,
    public currentPlayerId?: string,
    public createdAt: Date = new Date(),
    public startedAt?: Date,
    public finishedAt?: Date,
    public winnerId?: string
  ) {}

  // Métodos utilitarios
  isWaitingForPlayers(): boolean {
    return this.status === GameState.WAITING_FOR_PLAYERS;
  }

  isInProgress(): boolean {
    return this.status === GameState.NORMAL_PLAY;
  }

  isFinished(): boolean {
    return this.status === GameState.FINISHED;
  }

  hasSlot(): boolean {
    return this.players.length < this.maxPlayers;
  }

  getCurrentPlayer(): GamePlayer | undefined {
    return this.players.find(p => p.id === this.currentPlayerId);
  }

  getPlayerByUserId(userId: string): GamePlayer | undefined {
    return this.players.find(p => p.userId === userId);
  }

  getHumanPlayers(): GamePlayer[] {
    return this.players.filter(p => !p.isBot);
  }

  getBotPlayers(): GamePlayer[] {
    return this.players.filter(p => p.isBot);
  }

  isUserInGame(userId: string): boolean {
    return this.players.some(p => p.userId === userId);
  }

  canStart(): boolean {
    return this.players.length >= 2 && this.status === GameState.WAITING_FOR_PLAYERS;
  }

  // Crear desde datos del backend
  static fromApiData(data: any): GameModel {
    const game = new GameModel();

    game.id = data.id || data.gameId || '';
    game.gameCode = data.gameCode || data.code || '';
    game.createdBy = data.createdBy || data.createdByUsername || '';
    game.status = data.status || GameState.WAITING_FOR_PLAYERS;
    game.maxPlayers = data.maxPlayers || 6;
    game.turnTimeLimit = data.turnTimeLimit || 10;
    game.chatEnabled = data.chatEnabled ?? true;
    game.pactsAllowed = data.pactsAllowed ?? false;
    game.currentTurn = data.currentTurn || 0;
    game.currentPhase = data.currentPhase;
    game.currentPlayerId = data.currentPlayerId;
    game.winnerId = data.winnerId;

    // Fechas
    game.createdAt = data.createdAt ? new Date(data.createdAt) : new Date();
    game.startedAt = data.startedAt ? new Date(data.startedAt) : undefined;
    game.finishedAt = data.finishedAt ? new Date(data.finishedAt) : undefined;

    // Jugadores
    game.players = (data.players || []).map((p: any) => ({
      id: p.id,
      userId: p.userId,
      username: p.username || p.displayName || 'Usuario',
      isBot: p.isBot || false,
      botLevel: p.botLevel,
      color: p.color || '#000000',
      status: p.status || 'WAITING',
      seatOrder: p.seatOrder || 0,
      joinedAt: p.joinedAt ? new Date(p.joinedAt) : new Date(),
      territoriesCount: p.territoriesCount || 0,
      totalArmies: p.totalArmies || 0,
      cardsCount: p.cardsCount || 0,
      armiesToPlace: p.armiesToPlace || 0
    }));

    return game;
  }

  // Convertir a formato para backend
  toApiData(): any {
    return {
      id: this.id,
      gameCode: this.gameCode,
      createdBy: this.createdBy,
      status: this.status,
      maxPlayers: this.maxPlayers,
      turnTimeLimit: this.turnTimeLimit,
      chatEnabled: this.chatEnabled,
      pactsAllowed: this.pactsAllowed,
      currentTurn: this.currentTurn,
      currentPhase: this.currentPhase,
      currentPlayerId: this.currentPlayerId,
      players: this.players
    };
  }

  // Método para actualizar desde datos del backend
  updateFromApiData(data: any): void {
    const updated = GameModel.fromApiData(data);
    Object.assign(this, updated);
  }
}
