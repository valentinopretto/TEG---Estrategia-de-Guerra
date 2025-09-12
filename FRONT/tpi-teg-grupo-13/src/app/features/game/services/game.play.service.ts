// game.play.service.ts - Versión Limpia
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

// Main Interfaces
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
  startedAt: string;
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


// export enum GameState {
//   WAITING_FOR_PLAYERS = "WAITING_FOR_PLAYERS",
//   REINFORCEMENT_5 = "REINFORCEMENT_5",
//   REINFORCEMENT_3 = "REINFORCEMENT_3",
//   HOSTILITY_ONLY = "HOSTILITY_ONLY",
//   NORMAL_PLAY = "NORMAL_PLAY", // juego normal
//   PAUSED = "PAUSED",
//   FINISHED = "FINISHED"
// }


@Injectable({
  providedIn: 'root'
})
export class GamePlayService {
  private readonly API_URL = `${environment.apiUrl}/games`;
  private readonly COMBAT_API_URL = `${environment.apiUrl}/games/{gameCode}/combat`;
  private readonly CARD_API_URL = `${environment.apiUrl}/cards`;

  private gameStateSubject = new BehaviorSubject<GameResponseDto | null>(null);
  public gameState$ = this.gameStateSubject.asObservable();

  private isProcessingBotTurn = false;

  constructor(private http: HttpClient) {
  }

  // // Método principal para manejar turnos de bots
  // public handleBotTurnIfNeeded(gameCode: string): Observable<void> {
  //   const currentGame = this.gameStateSubject.value;
  //   if (!currentGame) {
  //     return new Observable(observer => observer.complete());
  //   }
  //
  //   const currentPlayer = currentGame.players[currentGame.currentPlayerIndex];
  //
  //   if (currentPlayer?.isBot && !this.isProcessingBotTurn && !currentGame.isGameOver) {
  //     console.log(`🤖 Ejecutando turno del bot: ${currentPlayer.username} - Fase: ${currentGame.currentPhase}`);
  //
  //     const botId = this.getBotId(currentPlayer);
  //     if (botId) {
  //       return this.executeBotTurn(botId, gameCode);
  //     }
  //   }
  //
  //   return new Observable(observer => observer.complete());
  // }

  // Método helper para obtener el ID del bot
  private getBotId(player: PlayerResponseDto): number | null {
    // Aquí necesitas obtener el ID del jugador
    // Esto depende de cómo esté estructurado tu PlayerResponseDto
    // Si no tienes el ID en el PlayerResponseDto, necesitarás agregarlo
    return player.id || null;
  }

  //Metodo para obtener el estado del juego por código
  public getGameState(gameCode: string): Observable<GameResponseDto> {
    return this.http.get<GameResponseDto>(`${this.API_URL}/${gameCode}`);
  }

  // // Ejecutar turno del bot
  // private executeBotTurn(botId: number, gameCode: string): Observable<void> {
  //   this.isProcessingBotTurn = true;
  //
  //   return new Observable<void>(observer => {
  //     // Llamar a tu endpoint del BotController
  //     this.http.post<GameResponseDto>(
  //       `${this.API_URL}/${gameCode}/${botId}/execute-turn`,
  //       {} // El endpoint no necesita body, todo va en la URL
  //     ).subscribe({
  //       next: async (response) => {
  //         try {
  //           if (response) {
  //             // Animar los cambios antes de actualizar completamente
  //             await this.animateGameChanges(this.gameStateSubject.value!, response);
  //
  //             // Actualizar el estado completo
  //             this.updateGameState(response);
  //
  //             // Si el siguiente jugador también es bot, ejecutar su turno después de un delay
  //             setTimeout(() => {
  //               this.handleBotTurnIfNeeded(gameCode).subscribe();
  //             }, 2000); // 2 segundos de pausa entre turnos de bots
  //           }
  //           observer.next();
  //           observer.complete();
  //         } catch (error) {
  //           console.error('Error procesando respuesta del bot:', error);
  //           observer.error(error);
  //         } finally {
  //           this.isProcessingBotTurn = false;
  //         }
  //       },
  //       error: (error) => {
  //         console.error('Error ejecutando turno del bot:', error);
  //         this.isProcessingBotTurn = false;
  //         observer.error(error);
  //       }
  //     });
  //   });
  // }

  // // Animar los cambios entre el estado anterior y el nuevo
  // private async animateGameChanges(oldState: GameResponseDto, newState: GameResponseDto): Promise<void> {
  //   // Detectar cambios en territorios (conquistas, refuerzos)
  //   const territoryChanges = this.detectTerritoryChanges(oldState, newState);
  //
  //   // Animar cada cambio con un delay
  //   for (const change of territoryChanges) {
  //     await this.animateTerritoryChange(change);
  //     await this.delay(800); // 800ms entre cada animación
  //   }
  // }
  //
  // // Detectar qué territorios cambiaron - VERSIÓN CORREGIDA
  // private detectTerritoryChanges(oldState: GameResponseDto, newState: GameResponseDto): TerritoryChange[] {
  //   const changes: TerritoryChange[] = [];
  //
  //   // Los territorios vienen como Map<number, CountryResponseDto>
  //   const oldTerritories = oldState.territories;
  //   const newTerritories = newState.territories;
  //
  //   // Iterar correctamente sobre el Map
  //   newTerritories.forEach((newTerritory, territoryId) => {
  //     const oldTerritory = oldTerritories.get(territoryId);
  //
  //     if (oldTerritory) {
  //       // Cambio de dueño (conquista)
  //       if (oldTerritory.ownerName !== newTerritory.ownerName) {
  //         changes.push({
  //           type: 'conquest',
  //           territoryName: newTerritory.name,
  //           territoryId: territoryId,
  //           oldOwner: oldTerritory.ownerName,
  //           newOwner: newTerritory.ownerName,
  //           newArmyCount: newTerritory.armies
  //         });
  //       }
  //       // Cambio en cantidad de ejércitos (refuerzos o movimientos)
  //       else if (oldTerritory.armies !== newTerritory.armies && newState.currentPhase === TurnPhase.REINFORCEMENT) {
  //         changes.push({
  //           type: 'reinforcement',
  //           territoryName: newTerritory.name,
  //           territoryId: territoryId,
  //           oldArmyCount: oldTerritory.armies,
  //           newArmyCount: newTerritory.armies,
  //           owner: newTerritory.ownerName
  //         });
  //       } else if (oldTerritory.armies !== newTerritory.armies && newState.currentPhase === TurnPhase.FORTIFY) {
  //         changes.push({
  //           type: 'fortify',
  //           territoryName: newTerritory.name,
  //           territoryId: territoryId,
  //           oldArmyCount: oldTerritory.armies,
  //           newArmyCount: newTerritory.armies,
  //           owner: newTerritory.ownerName
  //         });
  //       }
  //     }
  //   });
  //
  //   return changes;
  // }

  // // Animar un cambio específico en un territorio
  // private async animateTerritoryChange(change: TerritoryChange): Promise<void> {
  //   // Aquí puedes emitir eventos para que los componentes Territory se animen
  //   console.log(`🎯 Animando cambio en ${change.territoryName}:`, change);
  //
  //   // Ejemplo: hacer que el territorio parpadee o cambie de color temporalmente
  //   // Usando el ID del territorio para ser más específico
  //   const territoryElement = document.querySelector(`[data-territory-id="${change.territoryId}"]`);
  //   if (territoryElement) {
  //     territoryElement.classList.add('territory-changing');
  //
  //     // Mostrar mensaje de la acción
  //     if (change.type === 'conquest') {
  //       console.log(`⚔️ ${change.newOwner} conquistó ${change.territoryName} de ${change.oldOwner}`);
  //     } else if (change.type === 'reinforcement') {
  //       console.log(`🛡️ ${change.owner} reforzó ${change.territoryName}: ${change.oldArmyCount} → ${change.newArmyCount}`);
  //     } else if (change.type === 'fortify') {
  //       console.log(`🏰 ${change.owner} fortificó ${change.territoryName}: ${change.oldArmyCount} → ${change.newArmyCount}`);
  //     }
  //
  //     await this.delay(500);
  //     territoryElement.classList.remove('territory-changing');
  //   }
  // }

  // Actualizar el estado completo del juego
  private updateGameState(newState: GameResponseDto): void {
    this.gameStateSubject.next(newState);
    console.log('🎮 Estado del juego actualizado:', newState);
  }

  // Método para inicializar o actualizar el juego manualmente
  public updateGame(gameState: GameResponseDto): void {
    this.updateGameState(gameState);
  }

  // Obtener el estado actual
  public getCurrentGameState(): GameResponseDto | null {
    return this.gameStateSubject.value;
  }

  // Utility
  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }


  /** POST: coloca los ejércitos iniciales */
  placeInitialArmies(gameCode: string, payload: {
    playerId: number,
    armiesByCountry: { [countryId: string]: number }
  }): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/initial-placement/place-armies`,
      payload
    );
  }

  /** GET: resumen de la colocación inicial */
  getInitialPlacementSummary(gameCode: string): Observable<InitialPlacementSummary> {
    return this.http
      .get<RawInitialPlacementSummary>(`${this.API_URL}/${gameCode}/initial-placement/summary`)
      .pipe(
        map(raw => {
          // 1) Busca al jugador actual
          const me = raw.players.find(p => p.isCurrentPlayer)!;

          // 2) Construye armiesByCountry a partir de las tropas actuales
          const armiesByCountry = me.territories.reduce(
            (acc, t) => ((acc[t.id] = t.armies), acc),
            {} as Record<number, number>
          );

          // 3) Lista simplificada de países
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

  getFortifiableTerritories(gameCode: string, playerId: number) {
    return this.http.get<Territory[]>(`${this.API_URL}/${gameCode}/fortification/fortifiable-territories/${playerId}`);
  }

  getFortifyTargets(gameCode: string, fromTerritoryId: number, playerId: number) {
    return this.http.get<Territory[]>(`${this.API_URL}/${gameCode}/fortification/fortification-targets/${fromTerritoryId}/${playerId}`);
  }

  fortify(gameCode: string, dto: any) {
    return this.http.post(`${this.API_URL}/${gameCode}/fortification/fortify`, dto);
  }

  // Combat methods using COMBAT_API_URL
  public attackTerritory(gameCode: string, combatDto: AttackDto): Observable<CombatResultDto> {
    const url = this.COMBAT_API_URL.replace('{gameCode}', gameCode);
    return this.http.post<CombatResultDto>(`${url}/attack`, combatDto);
  }

  public getAttackableTargets(gameCode: string, fromTerritoryId: number): Observable<Territory[]> {
    const url = this.COMBAT_API_URL.replace('{gameCode}', gameCode);
    return this.http.get<Territory[]>(`${url}/attackable-targets/${fromTerritoryId}`);
  }

  public simulateCombat(gameCode: string, attackingArmies: number, defendingArmies: number): Observable<CombatSimulationDto> {
    return this.http.post<CombatSimulationDto>(
      `${this.COMBAT_API_URL.replace('{gameCode}', gameCode)}/simulate`,
      { attackingArmies, defendingArmies }
    );
  }

  /**
   * Finaliza el turno del jugador actual y pasa al siguiente jugador
   * @param gameCode Código del juego
   * @param playerId ID del jugador que quiere finalizar su turno
   * @returns Observable con el estado actualizado del juego
   */
  public endTurn(gameCode: string, playerId: number): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/turn/skip-attack/${playerId}`,
      {}
    ).pipe(
      switchMap(() =>
        this.http.post<GameResponseDto>(
          `${this.API_URL}/${gameCode}/turn/skip-fortify/${playerId}`,
          {}
        )
      ),
      switchMap(() =>
        this.http.post<GameResponseDto>(
          `${this.API_URL}/${gameCode}/turn/end-turn/${playerId}`,
          {}
        )
      )
    );
  }

  /*
  * Servicio para terminar el turno desde la fase claim_card
  */
  public endTurnFromClaimCard(gameCode: string, playerId: number): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/turn/end-turn/${playerId}`,
      {}
    );
  }

  /*
  * Servicio para terminar el turno desde la fase end_turn
  */
  public endTurnFromEndTurnPhase(gameCode: string, playerId: number, action: string): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/turn/action`,
      {
        playerId: playerId,
        gameId: 0, //El backend no usa este dato en su logica
        action: action
      }
    );
  }

  public claimCard(gameCode: string, playerId: number): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.CARD_API_URL}/give-card/`,
      {
        gameCode: gameCode,
        playerId: playerId
      }
    );
  }

  /**
   * Permite al jugador cambiar desde la fase de ataque a la fase de fortificación
   * @param gameCode Código del juego
   * @param playerId ID del jugador
   * @returns Observable con el estado actualizado del juego
   */
  public proceedToFortify(gameCode: string, playerId: number): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/turn/proceed-to-fortify/${playerId}`,
      {}
    );
  }

  /**
   * Permite al jugador saltar la fase de refuerzo
   * @param gameCode Código del juego
   * @param playerId ID del jugador
   * @returns Observable con el estado actualizado del juego
   */
  public skipReinforcement(gameCode: string, playerId: number): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/turn/skip-reinforcement/${playerId}`,
      {}
    );
  }

  /**
   * Permite al jugador saltar la fase de fortificación
   * @param gameCode Código del juego
   * @param playerId ID del jugador
   * @returns Observable con el estado actualizado del juego
   */
  public skipFortify(gameCode: string, playerId: number): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/turn/skip-fortify/${playerId}`,
      {}
    );
  }

  /**
   * Ejecuta una acción genérica de turno usando el endpoint unificado
   * @param gameCode Código del juego
   * @param playerId ID del jugador
   * @param action Acción a ejecutar (end_turn, skip_attack, skip_fortify, proceed_to_fortify, etc.)
   * @returns Observable con el estado actualizado del juego
   */
  public performTurnAction(gameCode: string, playerId: number, action: string): Observable<GameResponseDto> {
    const actionDto: TurnActionDto = {
      playerId: playerId,
      gameId: 0, //El backend no usa este dato en su logica
      action: action
    };

    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/turn/action`,
      actionDto
    );
  }


  calculateReinforcementArmies(gameCode: string, playerId: number | null): Observable<reinforcementNormalPlay> {
    return this.http.get<reinforcementNormalPlay>(
      `${this.API_URL}/${gameCode}/reinforcement/status/${playerId}`
    );
  }

  placeReinforcementArmies(gameCode: string, payload: {
    playerId: number,
    armiesByCountry: { [countryId: number]: number }
  }): Observable<GameResponseDto> {
    return this.http.post<GameResponseDto>(
      `${this.API_URL}/${gameCode}/reinforcement/place-armies`,
      payload
    );
  }


}
export interface RawInitialPlacementSummary {
  gameCode:        string;
  currentPhase:    string;
  isActive:        boolean;
  message:         string;
  currentPlayerId: number;
  expectedArmies:  number;
  players: Array<{
    playerId:       number;
    playerName:     string;
    seatOrder:      number;
    armiesToPlace:  number;
    territoryCount: number;
    isCurrentPlayer:boolean;
    territories: Array<{
      id:            number;
      name:          string;
      continentName: string;
      armies:        number;
      positionX:     number;
      positionY:     number;
    }>;
  }>;
}
export interface reinforcementNormalPlay{

  playerId: number,
  playerName: string,
  gameState: GameState,
  currentPhase: TurnPhase,
  armiesToPlace: number,
  baseArmies: number,
  continentBonus: number,
  cardBonus: number,
  totalArmies: number,
  isPlayerTurn: boolean,
  canReinforce: boolean,
  message: string,
  ownedTerritories: [ {id: number, name: string,continentName: string,armies: number, positionX: number, positionY: number}],
  controlledContinents: [ string]
}




export interface InitialPlacementSummary {
  availableArmies: number;
  armiesByCountry: { [countryId: number]: number };
  countries:       { id: number; name: string; currentArmies: number }[];
}
// no se que ta pasando ayuda holaaaaaaaaaaa
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

// Interface definitions for the service

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


// New DTO for generic turn actions
export interface TurnActionDto {
  playerId: number;
  gameId: number;
  action: string; // "end_turn", "skip_phase", "proceed_to_fortify", etc.
}
