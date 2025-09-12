import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AttackAction, AttackResult } from '../../../core/models/class/game-action.model';
import { NotificationService } from '../../../core/services/notification.service';

export interface AttackRequestDto {
  playerId: number;
  attackerCountryId: number;
  defenderCountryId: number;
  attackingArmies: number
}

export interface AttackResponseDto {
  attackerCountryId: number;
  attackerCountryName: string;
  defenderCountryId: number;
  defenderCountryName: string;
  attackerPlayerName: string;
  defenderPlayerName: string;
  attackerDice: number[];
  defenderDice: number[];
  attackerLosses: number;
  defenderLosses: number;
  territoryConquered: boolean;
  attackerRemainingArmies: number;
  defenderRemainingArmies: number;
}

export interface TerritoryDto {
  id: number;
  name: string;
  continentName: string;
  ownerId: number;
  ownerName: string;
  armies: number;
  lastConqueredTurn?: number;
  positionX: number;
  positionY: number;
  neighborIds: Set<number>;
  canAttack: boolean;
  canBeAttacked: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AttackService {
  private apiUrl = 'http://localhost:8080/api/games';

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  /**
   * Obtener territorios atacables del jugador (con más de 1 ejército)
   */
  getPlayerAttackableTerritories(gameCode: string, playerId: number): Observable<TerritoryDto[]> {
    return this.http.get<TerritoryDto[]>(`${this.apiUrl}/${gameCode}/combat/attackable-territories/${playerId}`);
  }

  /**
   * Obtener territorios enemigos que puede atacar un territorio específico
   */
  getAttackTargets(gameCode: string, territoryId: number, playerId: number): Observable<TerritoryDto[]> {
    return this.http.get<TerritoryDto[]>(`${this.apiUrl}/${gameCode}/combat/attack-targets/${territoryId}/${playerId}`);
  }

  /**
   * Ejecutar un ataque entre territorios
   */
  executeAttack(attackRequest: AttackRequestDto, gameCode: string): Observable<AttackResponseDto> {
    
    return this.http.post<AttackResponseDto>(
      `${this.apiUrl}/${gameCode}/combat/attack`,
      attackRequest
    );
  }

  /**
   * Obtener territorios que pueden atacar desde un territorio específico
   */
  getAttackableTerritories(gameCode: string, territoryId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${gameCode}/territories/${territoryId}/attackable`);
  }

  /**
   * Obtener territorios desde los cuales se puede atacar un territorio específico
   */
  getAttackingTerritories(gameCode: string, territoryId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${gameCode}/territories/${territoryId}/attackers`);
  }

  /**
   * Validar si un ataque es válido
   */
  validateAttack(
    attackerTerritory: any,
    defenderTerritory: any,
    attackingArmies: number
  ): { isValid: boolean; message?: string } {
    
    // Verificar que hay suficientes ejércitos para atacar
    if (attackingArmies >= attackerTerritory.armies) {
      return { isValid: false, message: 'Debes dejar al menos 1 ejército en el territorio atacante' };
    }

    // Verificar que hay al menos 1 ejército para atacar
    if (attackingArmies < 1) {
      return { isValid: false, message: 'Debes atacar con al menos 1 ejército' };
    }

    return { isValid: true };
  }

  /**
   * Calcular el número máximo de dados que puede usar el atacante
   */
  calculateMaxAttackerDice(attackingArmies: number): number {
    return Math.min(3, attackingArmies);
  }

  /**
   * Calcular el número máximo de dados que puede usar el defensor
   */
  calculateMaxDefenderDice(defenderArmies: number): number {
    return Math.min(2, defenderArmies);
  }

  /**
   * Mostrar notificación de resultado de ataque
   */
  showAttackResult(result: AttackResult): void {
    if (result.territoryConquered) {
      this.notificationService.notifyAttackResult(
        result.attackerPlayerName,
        result.defenderPlayerName,
        result.attackerCountryName,
        result.defenderCountryName,
        true
      );
    } else {
      this.notificationService.notifyAttackResult(
        result.attackerPlayerName,
        result.defenderPlayerName,
        result.attackerCountryName,
        result.defenderCountryName,
        false
      );
    }
  }

  /**
   * Crear una acción de ataque
   */
  createAttackAction(
    attackerTerritoryId: number,
    defenderTerritoryId: number,
    attackingArmies: number
  ): AttackAction {
    return {
      attackerCountryId: attackerTerritoryId.toString(),
      defenderCountryId: defenderTerritoryId.toString(),
      attackingArmies,
    };
  }
} 