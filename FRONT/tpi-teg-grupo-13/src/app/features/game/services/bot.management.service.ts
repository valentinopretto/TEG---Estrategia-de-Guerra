// game.play.service.ts - Versión Corregida y Simplificada
import {Injectable, OnDestroy} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, Observable, Subscription, map, switchMap} from 'rxjs';
import {ChatMessageResponseDto} from '../../chat/services/chat.service';
import {EventType} from '../../../core/enums/event-type';
import {CardType} from '../../../core/enums/card-type';
import {ObjectiveType} from '../../../core/enums/objective-type';
import {TurnPhase} from '../../../core/enums/turn-phase';
import {Territory} from '../../../core/models/interfaces/map';
import {environment} from '../../../../environments/environment';
import {NotificationService} from '../../../core/services/notification.service';

// Enums
export enum GameState {
  WAITING_FOR_PLAYERS = "WAITING_FOR_PLAYERS",
  REINFORCEMENT_5 = "REINFORCEMENT_5",
  REINFORCEMENT_3 = "REINFORCEMENT_3",
  HOSTILITY_ONLY = "HOSTILITY_ONLY",
  NORMAL_PLAY = "NORMAL_PLAY",
  PAUSED = "PAUSED",
  FINISHED = "FINISHED"
}

// [Mantener todas las interfaces existentes...]
export interface GameResponseDto {
  id: number;
  gameCode: string;
  createdByUsername: string;
  state: GameState;
  currentPhase: TurnPhase;
  currentTurn: number;
  currentPlayerIndex: number;
  maxPlayers: number;
  turnTimeLimit: number;
  chatEnabled: boolean;
  pactsAllowed: boolean;
  createdAt: string;
  startedAt?: string;
  finishedAt?: string;
  players: PlayerResponseDto[];
  territories: Map<number, CountryResponseDto>;
  continents: ContinentResponseDto[];
  recentEvents: GameEventDto[];
  recentMessages: ChatMessageResponseDto[];
  currentPlayerName: string;
  canStart: boolean;
  isGameOver: boolean;
  winnerName?: string;
}

export interface PlayerResponseDto {
  id: number;
  username: string;
  displayName: string;
  status: string;
  color: string;
  isBot: boolean;
  botLevel: string;
  armiesToPlace: number;
  seatOrder: number;
  joinedAt: string;
  eliminatedAt: string;
  hand: CardResponseDto[];
  territoryIds: number[];
  objetive: ObjetiveResponseDto;
  territoryCount: number;
  totalArmies: number;
}

export interface ObjetiveResponseDto {
  id: number;
  description: string;
  isAchieved: boolean;
  isCommon: boolean;
  type: ObjectiveType
}

export interface CardResponseDto {
  id: number;
  countryName: string;
  type: CardType;
  isInDeck: boolean;
}

export interface CountryResponseDto {
  id: number;
  name: string;
  continentName: string;
  ownerName: string;
  armies: number;
  positionX: number;
  positionY: number;
  neightborIds: Set<number>;
  canBeAttacked: boolean;
  canAttack: boolean;
}

export interface ContinentResponseDto {
  id: number;
  name: string;
  bonusArmies: number;
  countries: CountryResponseDto[];
  controllerName: string;
  isControlled: boolean;
  totalCountries: number;
  controlledCountries: number;
}

interface GameEventDto {
  id: number;
  turnNumber: number;
  actorName: string;
  type: EventType;
  description: string;
  data: string;
  timestamp: Date;
}

export interface AttackDto {
  fromTerritoryId: number;
  toTerritoryId: number;
  attackingArmies: number;
}

export interface CombatResultDto {
  success: boolean;
  attackerLosses: number;
  defenderLosses: number;
  territoryConquered: boolean;
  newTerritoryOwner?: string;
  remainingAttackingArmies: number;
  remainingDefendingArmies: number;
  diceResults: {
    attackerDice: number[];
    defenderDice: number[];
  };
}

export interface CombatSimulationDto {
  attackerWinProbability: number;
  defenderWinProbability: number;
  averageAttackerLosses: number;
  averageDefenderLosses: number;
  possibleOutcomes: {
    attackerLosses: number;
    defenderLosses: number;
    probability: number;
  }[];
}

export interface RawInitialPlacementSummary {
  gameCode: string;
  currentPhase: string;
  isActive: boolean;
  message: string;
  currentPlayerId: number;
  expectedArmies: number;
  players: Array<{
    playerId: number;
    playerName: string;
    seatOrder: number;
    armiesToPlace: number;
    territoryCount: number;
    isCurrentPlayer: boolean;
    territories: Array<{
      id: number;
      name: string;
      continentName: string;
      armies: number;
      positionX: number;
      positionY: number;
    }>;
  }>;
}

export interface InitialPlacementSummary {
  availableArmies: number;
  armiesByCountry: { [countryId: number]: number };
  countries: { id: number; name: string; currentArmies: number }[];
}

interface TerritoryChange {
  type: 'conquest' | 'reinforcement' | 'fortify';
  territoryName: string;
  territoryId: number;
  oldOwner?: string;
  newOwner?: string;
  oldArmyCount?: number;
  newArmyCount?: number;
  owner?: string;
}

@Injectable({
  providedIn: 'root'
})
export class BotManagementService implements OnDestroy {
  // API URLs
  private readonly API_URL = `${environment.apiUrl}/games`;
  private readonly COMBAT_API_URL = `${environment.apiUrl}/games/{gameCode}/combat`;
  private readonly BOT_API_URL = `${environment.apiUrl}/bots/games`;

  // State Management - SIMPLIFICADO
  private gameStateSubject = new BehaviorSubject<GameResponseDto | null>(null);
  public gameState$ = this.gameStateSubject.asObservable();

  // Bot Management - SIMPLIFICADO
  private isProcessingBotTurn = false;
  private botTurnSubscription?: Subscription;
  private pollingSubscription?: Subscription;

  // Variables para tracking de estado - REDUCIDAS
  private lastProcessedPlayerIndex: number | null = null;
  private lastProcessedTurn: number | null = null;

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  // ==============================================
  // CORE GAME STATE MANAGEMENT - SIMPLIFICADO
  // ==============================================

  /**
   * Obtiene el estado actual del juego
   */
  public getGameState(gameCode: string): Observable<GameResponseDto> {
    const url = `${this.API_URL}/${gameCode}`;
    console.log('📡 Obteniendo estado del juego desde:', url);
    return this.http.get<GameResponseDto>(url);
  }

  /**
   * Actualizar estado del juego - VERSIÓN SIMPLIFICADA
   */
  public updateGameState(newState: GameResponseDto): void {
    console.log('🔄 Actualizando estado del juego...');

    if (!newState || !newState.players || newState.players.length === 0) {
      console.log('❌ Estado inválido recibido');
      return;
    }

    // SIEMPRE actualizar el Subject primero
    this.gameStateSubject.next(newState);
    console.log('✅ Estado actualizado en Subject');

    // Verificar si es necesario procesar bot - LÓGICA SIMPLIFICADA
    this.checkIfBotTurnNeeded(newState);
  }

  /**
   * NUEVA FUNCIÓN SIMPLIFICADA para verificar turno de bot
   */
  private checkIfBotTurnNeeded(gameState: GameResponseDto): void {
    console.log('🤖 Verificando si es turno de bot...');

    // Verificaciones básicas
    if (gameState.isGameOver) {
      console.log('🏁 Juego terminado, no procesar bots');
      return;
    }

    if (this.isProcessingBotTurn) {
      console.log('⏳ Ya hay un bot procesándose');
      return;
    }

    const currentPlayer = gameState.players[gameState.currentPlayerIndex];
    if (!currentPlayer) {
      console.log('❌ No se encontró jugador actual');
      return;
    }

    console.log(`👤 Jugador actual: ${currentPlayer.displayName} (Bot: ${currentPlayer.isBot})`);

    // Verificar si cambió el turno o es la primera vez
    const turnChanged = (
      this.lastProcessedPlayerIndex !== gameState.currentPlayerIndex ||
      this.lastProcessedTurn !== gameState.currentTurn
    );

    if (!turnChanged) {
      console.log('📊 No hay cambio de turno');
      return;
    }

    // Actualizar tracking
    this.lastProcessedPlayerIndex = gameState.currentPlayerIndex;
    this.lastProcessedTurn = gameState.currentTurn;

    // Si es bot y estado válido, ejecutar
    if (currentPlayer.isBot && this.isValidStateForBotExecution(gameState.state)) {
      console.log('🚀 ¡Ejecutando turno de bot!');
      // Pequeño delay para evitar race conditions
      setTimeout(() => {
        this.executeBotTurn(currentPlayer.id, gameState.gameCode);
      }, 100);
    } else {
      console.log('👤 Es turno de jugador humano o estado no válido');
    }
  }

  /**
   * Método público para actualizar juego externamente
   */
  public updateGame(gameState: GameResponseDto): void {
    this.updateGameState(gameState);
  }

  /**
   * Obtener estado actual
   */
  public getCurrentGameState(): GameResponseDto | null {
    return this.gameStateSubject.value;
  }

  // ==============================================
  // BOT MANAGEMENT SYSTEM - SIMPLIFICADO
  // ==============================================

  /**
<<<<<<< HEAD
   * Método principal para manejar turnos de bots
   */
  public handleBotTurnIfNeeded(gameCode: string): void {
    console.log('🤖 ================================');
    console.log('🤖 VERIFICANDO TURNO DE BOT');
    console.log('🤖 ================================');
    console.log('🤖 GameCode recibido:', gameCode);

    let currentGame: any = null;
    this.getGameState(gameCode).subscribe({
      next: (gamaState) =>{
          currentGame = gamaState;
      }
    })
    console.log('🎮 Estado del juego completo:', currentGame);

    if (!currentGame) {
      console.log('❌ SALIDA: No hay estado del juego disponible');
      return;
    }

    console.log('🎮 Detalles del estado del juego:');
    console.log('   - gameCode:', currentGame.gameCode);
    console.log('   - state:', currentGame.state);
    console.log('   - phase:', currentGame.currentPhase);
    console.log('   - currentPlayerIndex:', currentGame.currentPlayerIndex);
    console.log('   - isGameOver:', currentGame.isGameOver);
    console.log('   - players count:', currentGame.players?.length);

    // if (currentGame.isGameOver) {
    //   console.log('🏁 SALIDA: El juego ha terminado');
    //
    // }

    const currentPlayer = currentGame.players[currentGame.currentPlayerIndex];
    console.log('👤 Jugador actual completo:', currentPlayer);

    if (!currentPlayer) {
      console.error('❌ SALIDA: No se encontró el jugador actual');
      console.error('   - Players array:', currentGame.players);
      console.error('   - Current index:', currentGame.currentPlayerIndex);
      return;
    }

    console.log('👤 Detalles del jugador actual:');
    console.log('   - id:', currentPlayer.id);
    console.log('   - displayName:', currentPlayer.displayName);
    console.log('   - username:', currentPlayer.username);
    console.log('   - isBot:', currentPlayer.isBot);
    console.log('   - tipo de isBot:', typeof currentPlayer.isBot);

    console.log('🔍 Verificando validez para ejecución...');
    console.log('   - Estado del juego:', currentGame.state);
    console.log('   - Es bot:', currentPlayer.isBot);
    console.log('   - isProcessingBotTurn:', this.isProcessingBotTurn);

    // Llamar al método de validación
    const isValidForExecution = this.isValidStateForBotExecution(currentGame.state);
    console.log('✅ Resultado de validación:', isValidForExecution);

    if (!isValidForExecution) {
      console.log(`⏸️ SALIDA: No es válido para ejecución`);
      console.log(`   - Estado: ${currentGame.state}`);
      console.log(`   - Jugador: ${currentPlayer.displayName}`);
      console.log(`   - Es bot: ${currentPlayer.isBot}`);
      return;
    }

    console.log('🎯 Verificando condiciones finales:');
    console.log('   - currentPlayer.isBot:', currentPlayer.isBot);
    console.log('   - !this.isProcessingBotTurn:', !this.isProcessingBotTurn);
    console.log('   - Ambas condiciones:', currentPlayer.isBot && !this.isProcessingBotTurn);

    if (currentPlayer.isBot && !this.isProcessingBotTurn) {
      console.log('🚀 ¡¡¡EJECUTANDO TURNO DEL BOT!!!');
      console.log(`   - Bot ID: ${currentPlayer.id}`);
      console.log(`   - Bot Name: ${currentPlayer.displayName}`);
      console.log(`   - Game Code: ${gameCode}`);
      this.executeBotTurn(currentPlayer.id, gameCode);
    } else if (!currentPlayer.isBot) {
      console.log('👤 SALIDA: Es turno de un jugador humano');
    } else {
      console.log('⏳ SALIDA: Ya hay un turno de bot procesándose');
      console.log('   - isProcessingBotTurn:', this.isProcessingBotTurn);
    }

    console.log('🤖 ================================');
    console.log('🤖 FIN VERIFICACIÓN');
    console.log('🤖 ================================');
  }

  /**
   * Ejecutar turno del bot
=======
   * Ejecutar turno del bot - VERSIÓN MEJORADA
>>>>>>> 11fd8f172274f1c3c1ed3a505e451aa73374e31b
   */
  public executeBotTurn(botId: number, gameCode: string): void {
    console.log('🚀 Ejecutando turno de bot:', { botId, gameCode });

    if (this.isProcessingBotTurn) {
      console.log('⏳ Ya hay un turno de bot en proceso');
      return;
    }

    this.isProcessingBotTurn = true;

    // Limpiar subscription anterior
    if (this.botTurnSubscription) {
      this.botTurnSubscription.unsubscribe();
    }

    const url = `${this.BOT_API_URL}/${gameCode}/${botId}/execute-turn`;
    console.log('📡 Llamando a:', url);

    this.botTurnSubscription = this.http.post<GameResponseDto>(url, {}).subscribe({
      next: async (gameResponse) => {
        console.log('✅ Turno de bot ejecutado exitosamente');

        try {
          // Animar cambios si hay estado previo
          const previousState = this.gameStateSubject.value;
          if (previousState) {
            await this.animateGameChanges(previousState, gameResponse);
          }

          // Mostrar notificación
          this.notificationService.showNotification(
            "success",
            `Turno de ${gameResponse.players[gameResponse.currentPlayerIndex].displayName} Finalizado`,
            "El bot ha ejecutado su turno exitosamente."
          );

          // Actualizar estado con datos frescos del servidor
          this.refreshGameState(gameCode);

        } catch (error) {
          console.error('❌ Error en post-procesamiento:', error);
        } finally {
          this.isProcessingBotTurn = false;
        }
      },
      error: (error) => {
        console.error('❌ Error ejecutando turno del bot:', error);
        this.isProcessingBotTurn = false;

        // Intentar recuperar estado
        this.refreshGameState(gameCode);
      }
    });
  }

  /**
   * NUEVA FUNCIÓN para refrescar estado desde servidor
   */
  private refreshGameState(gameCode: string): void {
    console.log('🔄 Refrescando estado desde servidor...');

    this.getGameState(gameCode).subscribe({
      next: (freshState) => {
        console.log('✅ Estado refrescado');
        this.updateGameState(freshState);
      },
      error: (error) => {
        console.error('❌ Error refrescando estado:', error);
      }
    });
  }

  /**
   * Verificar si el estado del juego permite ejecución de bot
   */
  private isValidStateForBotExecution(gameState: GameState): boolean {
    const validStates = [
      GameState.NORMAL_PLAY,
      GameState.REINFORCEMENT_5,
      GameState.REINFORCEMENT_3,
      GameState.HOSTILITY_ONLY
    ];

    const isValid = validStates.includes(gameState);
    console.log(`🎯 Estado ${gameState} ${isValid ? 'válido' : 'inválido'} para bot`);

    return isValid;
  }

  // ==============================================
  // ANIMATION SYSTEM - MANTENIDO
  // ==============================================

  private async animateGameChanges(oldState: GameResponseDto, newState: GameResponseDto): Promise<void> {
    const territoryChanges = this.detectTerritoryChanges(oldState, newState);

    for (const change of territoryChanges) {
      await this.animateTerritoryChange(change);
      await this.delay(800);
    }
  }

  private detectTerritoryChanges(oldState: GameResponseDto, newState: GameResponseDto): TerritoryChange[] {
    const changes: TerritoryChange[] = [];

    try {
      if (!oldState?.territories || !newState?.territories) {
        console.warn('⚠️ territories no definido');
        return [];
      }

      const oldTerritories = oldState.territories;
      const newTerritories = newState.territories;

      // Función helper para iterar territories
      const iterateTerritories = (territories: any, callback: (territory: any, id: string) => void) => {
        if (Array.isArray(territories)) {
          territories.forEach((territory, index) => callback(territory, index.toString()));
        } else if (territories instanceof Map) {
          territories.forEach((territory, id) => callback(territory, id.toString()));
        } else if (typeof territories === 'object') {
          Object.entries(territories).forEach(([id, territory]) => callback(territory, id));
        }
      };

      const getTerritory = (territories: any, id: string) => {
        if (Array.isArray(territories)) {
          return territories[parseInt(id)];
        } else if (territories instanceof Map) {
          return territories.get(id);
        } else if (typeof territories === 'object') {
          return territories[id];
        }
        return null;
      };

      iterateTerritories(newTerritories, (newTerritory, territoryId) => {
        if (!newTerritory) return;

        const oldTerritory = getTerritory(oldTerritories, territoryId);

        if (oldTerritory) {
          // Cambio de dueño
          if (oldTerritory.ownerName !== newTerritory.ownerName) {
            changes.push({
              type: 'conquest',
              territoryName: newTerritory.name,
              territoryId: parseInt(territoryId),
              oldOwner: oldTerritory.ownerName,
              newOwner: newTerritory.ownerName,
              newArmyCount: newTerritory.armies
            });
          }
          // Cambio en ejércitos
          else if (oldTerritory.armies !== newTerritory.armies) {
            const changeType = newState.currentPhase === TurnPhase.REINFORCEMENT ? 'reinforcement' : 'fortify';
            changes.push({
              type: changeType as 'reinforcement' | 'fortify',
              territoryName: newTerritory.name,
              territoryId: parseInt(territoryId),
              oldArmyCount: oldTerritory.armies,
              newArmyCount: newTerritory.armies,
              owner: newTerritory.ownerName
            });
          }
        }
      });

    } catch (error) {
      console.error('❌ Error detectando cambios:', error);
    }

    return changes;
  }

  private async animateTerritoryChange(change: TerritoryChange): Promise<void> {
    console.log(`🎯 Animando cambio en ${change.territoryName}:`, change);

    const territoryElement = document.querySelector(`[data-territory-id="${change.territoryId}"]`);
    if (territoryElement) {
      territoryElement.classList.add('territory-changing');

      if (change.type === 'conquest') {
        console.log(`⚔️ ${change.newOwner} conquistó ${change.territoryName}`);
      } else if (change.type === 'reinforcement') {
        console.log(`🛡️ Refuerzo en ${change.territoryName}: ${change.oldArmyCount} → ${change.newArmyCount}`);
      } else if (change.type === 'fortify') {
        console.log(`🏰 Fortificación en ${change.territoryName}: ${change.oldArmyCount} → ${change.newArmyCount}`);
      }

      await this.delay(500);
      territoryElement.classList.remove('territory-changing');
    }
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // ==============================================
  // INITIAL PLACEMENT METHODS - MANTENIDOS
  // ==============================================

  placeInitialArmies(gameCode: string, payload: {
    playerId: number,
    armiesByCountry: { [countryId: string]: number }
  }): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/initial-placement/place-armies`,
      payload
    );
  }

  getInitialPlacementSummary(gameCode: string): Observable<InitialPlacementSummary> {
    return this.http
      .get<RawInitialPlacementSummary>(`${this.API_URL}/${gameCode}/initial-placement/summary`)
      .pipe(
        map(raw => {
          const me = raw.players.find(p => p.isCurrentPlayer)!;
          const armiesByCountry = me.territories.reduce(
            (acc, t) => ((acc[t.id] = t.armies), acc),
            {} as Record<number, number>
          );
          const countries = me.territories.map(t => ({
            id: t.id,
            name: t.name,
            currentArmies: t.armies
          }));

          return {
            availableArmies: raw.expectedArmies,
            armiesByCountry,
            countries
          };
        })
      );
  }

  // ==============================================
  // MÉTODOS PÚBLICOS PARA DEBUG
  // ==============================================

  public debugCurrentState(): void {
    const currentState = this.gameStateSubject.value;
    console.log('🐛 DEBUG - Estado actual:', {
      hasState: !!currentState,
      isProcessingBot: this.isProcessingBotTurn,
      gameCode: currentState?.gameCode,
      gameState: currentState?.state,
      currentPlayerIndex: currentState?.currentPlayerIndex,
      currentPlayer: currentState?.players[currentState.currentPlayerIndex]?.displayName,
      isBot: currentState?.players[currentState.currentPlayerIndex]?.isBot,
      lastProcessedPlayerIndex: this.lastProcessedPlayerIndex,
      lastProcessedTurn: this.lastProcessedTurn
    });
  }

  /**
   * MÉTODO PÚBLICO para forzar verificación de bot (para testing)
   */
  public forceCheckBotTurn(gameCode: string): void {
    const currentState = this.getCurrentGameState();
    if (currentState) {
      this.checkIfBotTurnNeeded(currentState);
    } else {
      this.refreshGameState(gameCode);
    }
  }

  // ==============================================
  // CLEANUP
  // ==============================================

  public ngOnDestroy(): void {
    console.log('🧹 Limpiando BotManagementService...');

    if (this.botTurnSubscription) {
      this.botTurnSubscription.unsubscribe();
    }

    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }

    this.isProcessingBotTurn = false;
  }
}
