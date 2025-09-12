import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { AttackService, AttackRequestDto, TerritoryDto } from '../../services/attack.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { GamePlayService, CountryResponseDto } from '../../services/game.play.service';
import { AttackResult } from '../../../../core/models/class/game-action.model';

@Component({
  selector: 'app-attack-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './attack-modal.component.html',
  styleUrls: ['./attack-modal.component.css']
})
export class AttackModalComponent implements OnInit, OnChanges {
  @Input() gameCode: string = '';
  @Input() isVisible: boolean = false;
  @Output() closeModal = new EventEmitter<void>();
  @Output() attackExecuted = new EventEmitter<any>();

  // Territorios del jugador
  playerTerritories: TerritoryDto[] = [];

  //ID del jugador
  playerId: number = 0;

  // Territorios atacables
  attackableTerritories: TerritoryDto[] = [];

  // Territorios seleccionados
  selectedAttackerTerritory: TerritoryDto | null = null;
  selectedDefenderTerritory: TerritoryDto | null = null;
  
  // Estados
  isLoading: boolean = false;
  showAttackResults: boolean = false;
  
  // Resultados del ataque
  attackResult: AttackResult | null = null;
  
  // Referencias a territorios del juego
  gameTerritories: any = null;

  constructor(
    private attackService: AttackService,
    private notificationService: NotificationService,
    private gamePlayService: GamePlayService
  ) {}

  ngOnInit(): void {
    this.loadGameTerritories();
  }

  /**
   * Manejar cuando el modal se hace visible
   */
  ngOnChanges(): void {
    if (this.isVisible) {
      console.log('🚀 AttackModal: Modal abierto, cargando territorios del jugador');
      this.loadPlayerTerritories();
    }
  }

  /**
   * Cargar territorios del juego actual
   */
  private loadGameTerritories(): void {
    console.log('🔄 AttackModal: Iniciando carga de territorios');
    
    this.gamePlayService.gameState$.subscribe({
      next: (gameState) => {
        console.log('📡 AttackModal: Recibido gameState');
        
        if (gameState && gameState.territories) {
          this.playerId = gameState.players.find(player => player.username === this.getCurrentUser().username)?.id || 0;
          this.gameTerritories = gameState.territories;
          console.log('✅ AttackModal: Territorios asignados correctamente');
        } else {
          console.warn('⚠️ AttackModal: gameState o territories es null/undefined');
        }
      },
      error: (error) => {
        console.error('❌ AttackModal: Error en gameState$:', error);
      }
    });
  }

  /**
   * Cargar territorios atacables del jugador usando el endpoint
   */
  private loadPlayerTerritories(): void {
    // Obtener el jugador actual del localStorage
    const currentUser = this.getCurrentUser();
    if (!currentUser) {
      console.warn('⚠️ No se pudo obtener el usuario actual');
      this.playerTerritories = [];
      return;
    }

    // Obtener el ID del jugador
    const playerId = this.playerId;
    if (!playerId || playerId === 0) {
      console.warn('⚠️ No se pudo obtener el ID del jugador');
      this.playerTerritories = [];
      return;
    }

    // Llamar al endpoint para obtener territorios atacables
    this.attackService.getPlayerAttackableTerritories(this.gameCode, playerId)
      .subscribe({
        next: (territories) => {
          this.playerTerritories = territories;
          console.log('✅ Territorios atacables cargados:', this.playerTerritories.length);
        },
        error: (error) => {
          console.error('❌ Error al cargar territorios atacables:', error);
          this.playerTerritories = [];
          this.notificationService.showNotification(
            'error',
            'Error',
            'No se pudieron cargar los territorios atacables'
          );
        }
      });
  }

  /**
   * Obtener territorios del jugador actual (para el template)
   */
  getPlayerTerritories(): TerritoryDto[] {
    return this.playerTerritories;
  }

  /**
   * Manejar cambio en territorio atacante (desde select)
   */
  onAttackerTerritoryChange(): void {
    if (this.selectedAttackerTerritory) {
      this.selectedDefenderTerritory = null;
      this.loadAttackableTerritories();
      console.log('🎯 Territorio atacante seleccionado:', this.selectedAttackerTerritory.name);
    }
  }

  /**
   * Cargar territorios que pueden ser atacados desde el territorio seleccionado
   */
  private loadAttackableTerritories(): void {
    if (!this.selectedAttackerTerritory) {
      this.attackableTerritories = [];
      return;
    }

    // Llamar al endpoint para obtener territorios atacables
    this.attackService.getAttackTargets(this.gameCode, this.selectedAttackerTerritory.id, this.playerId)
      .subscribe({
        next: (territories) => {
          this.attackableTerritories = territories;
          console.log('✅ Territorios objetivo cargados:', this.attackableTerritories.length);
        },
        error: (error) => {
          console.error('❌ Error al cargar territorios objetivo:', error);
          this.attackableTerritories = [];
          this.notificationService.showNotification(
            'error',
            'Error',
            'No se pudieron cargar los territorios objetivo'
          );
        }
      });
  }

  /**
   * Manejar cambio en territorio defensor (desde select)
   */
  onDefenderTerritoryChange(): void {
    if (this.selectedDefenderTerritory) {
      console.log('🛡️ Territorio defensor seleccionado:', this.selectedDefenderTerritory.name);
    }
  }

  /**
   * Obtener el usuario actual desde localStorage
   */
  private getCurrentUser(): any {
    const storedUser = localStorage.getItem('teg_current_user');
    
    if (storedUser) {
      try {
        return JSON.parse(storedUser);
      } catch (error) {
        console.error('❌ Error parsing user data:', error);
      }
    } else {
      console.warn('⚠️ No se encontró usuario en localStorage');
    }
    return null;
  }

  /**
   * Manejar el envío del formulario
   */
  onSubmit(form: any): void {
    if (form.valid) {
      this.executeAttack();
    } else {
      this.notificationService.showNotification(
        'warning',
        'Formulario incompleto',
        'Por favor completa todos los campos requeridos'
      );
    }
  }

  /**
   * Validar el ataque antes de ejecutarlo
   */
  private validateAttack(): { isValid: boolean; message?: string } {
    if (!this.selectedAttackerTerritory || !this.selectedDefenderTerritory) {
      return { isValid: false, message: 'Debes seleccionar territorios atacante y defensor' };
    }

    // Verificar que el territorio atacante tiene más de 1 ejército
    if (this.selectedAttackerTerritory.armies <= 1) {
      return { isValid: false, message: 'El territorio atacante debe tener más de 1 ejército' };
    }

    return { isValid: true };
  }

  /**
   * Ejecutar el ataque
   */
  async executeAttack(): Promise<void> {
    const validation = this.validateAttack();
    if (!validation.isValid) {
      this.notificationService.showNotification('error', 'Error', validation.message || 'Ataque inválido');
      return;
    }

    this.isLoading = true;

    try {
      // Calcular automáticamente los valores según las reglas del juego
      const attackingArmies = this.selectedAttackerTerritory!.armies > 3 ? 3 : this.selectedAttackerTerritory!.armies;
      const attackerDice = Math.min(this.selectedAttackerTerritory!.armies - 1, 3);
      const defenderDice = Math.min(this.selectedDefenderTerritory!.armies, 3);

      const attackRequest: AttackRequestDto = {
        playerId: this.playerId,
        attackerCountryId: this.selectedAttackerTerritory!.id,
        defenderCountryId: this.selectedDefenderTerritory!.id,
        attackingArmies: 1
      };

      console.log('⚔️ Ejecutando ataque:', {
        from: this.selectedAttackerTerritory!.name,
        to: this.selectedDefenderTerritory!.name,
        attackingArmies,
        attackerDice,
        defenderDice
      });

      const response = await firstValueFrom(this.attackService.executeAttack(attackRequest, this.gameCode));
      
      console.log('📡 Respuesta del backend:', response);
      
      // La respuesta del backend viene directamente con los datos del ataque
      // Convertir la respuesta al formato AttackResult para el template
      this.attackResult = {
        attackerCountryId: response.attackerCountryId.toString(),
        attackerCountryName: response.attackerCountryName,
        defenderCountryId: response.defenderCountryId.toString(),
        defenderCountryName: response.defenderCountryName,
        attackerPlayerName: response.attackerPlayerName,
        defenderPlayerName: response.defenderPlayerName,
        attackerDice: response.attackerDice,
        defenderDice: response.defenderDice,
        attackerLosses: response.attackerLosses,
        defenderLosses: response.defenderLosses,
        territoryConquered: response.territoryConquered,
        attackerRemainingArmies: response.attackerRemainingArmies,
        defenderRemainingArmies: response.defenderRemainingArmies
      };
      
      this.showAttackResults = true;
      
      console.log('✅ Ataque ejecutado exitosamente:', this.attackResult);

    } catch (error: any) {
      console.error('Error ejecutando ataque:', error);
      this.notificationService.showNotification('error', 'Error', 'No se pudo ejecutar el ataque');
    } finally {
      this.isLoading = false;
    }
  }

  /**
   * Cerrar el modal y recargar el mapa
   */
  onClose(): void {
    this.closeModal.emit();
    // Emitir evento para recargar el mapa
    this.attackExecuted.emit();
    // Resetear estados
    this.showAttackResults = false;
    this.attackResult = null;
    this.selectedAttackerTerritory = null;
    this.selectedDefenderTerritory = null;
  }

  /**
   * Continuar después de ver los resultados
   */
  onContinue(): void {
    this.showAttackResults = false;
    this.attackResult = null;
    this.selectedAttackerTerritory = null;
    this.selectedDefenderTerritory = null;
  }

  // Hacer Math disponible en el template
  protected readonly Math = Math;
} 