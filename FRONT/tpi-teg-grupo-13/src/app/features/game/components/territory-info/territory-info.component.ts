import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CountryResponseDto } from '../../services/game.play.service';

@Component({
  selector: 'app-territory-info',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './territory-info.component.html',
  styleUrls: ['./territory-info.component.css']
})
export class TerritoryInfoComponent {
  @Input() territory: CountryResponseDto | null = null;
  @Input() isVisible: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() selectForAttack = new EventEmitter<CountryResponseDto>();
  @Output() selectForDefense = new EventEmitter<CountryResponseDto>();

  /**
   * Cerrar el panel de información
   */
  onClose(): void {
    this.close.emit();
  }

  /**
   * Seleccionar territorio para ataque
   */
  onSelectForAttack(): void {
    if (this.territory) {
      this.selectForAttack.emit(this.territory);
    }
  }

  /**
   * Seleccionar territorio para defensa
   */
  onSelectForDefense(): void {
    if (this.territory) {
      this.selectForDefense.emit(this.territory);
    }
  }

  /**
   * Obtener el color del propietario
   */
  getOwnerColor(): string {
    if (!this.territory) return '#666';
    
    // Mapear nombres de propietarios a colores
    const colorMap: { [key: string]: string } = {
      'Jugador 1': '#ff4444',
      'Jugador 2': '#4444ff',
      'Jugador 3': '#44ff44',
      'Jugador 4': '#ffff44',
      'Jugador 5': '#ff44ff',
      'Jugador 6': '#44ffff'
    };

    return colorMap[this.territory.ownerName] || '#666';
  }

  /**
   * Verificar si el territorio puede atacar
   */
  canAttack(): boolean {
    return this.territory?.canAttack || false;
  }

  /**
   * Verificar si el territorio puede ser atacado
   */
  canBeAttacked(): boolean {
    return this.territory?.canBeAttacked || false;
  }

  /**
   * Obtener el estado del territorio
   */
  getTerritoryStatus(): string {
    if (!this.territory) return '';

    if (this.territory.canAttack) {
      return 'Puede atacar';
    } else if (this.territory.canBeAttacked) {
      return 'Puede ser atacado';
    } else {
      return 'Neutral';
    }
  }

  /**
   * Obtener la clase CSS del estado
   */
  getStatusClass(): string {
    if (!this.territory) return 'status-neutral';

    if (this.territory.canAttack) {
      return 'status-attacker';
    } else if (this.territory.canBeAttacked) {
      return 'status-defender';
    } else {
      return 'status-neutral';
    }
  }
} 