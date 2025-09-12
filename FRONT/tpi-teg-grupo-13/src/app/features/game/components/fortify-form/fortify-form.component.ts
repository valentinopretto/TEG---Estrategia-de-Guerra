import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import {Territory} from '../../../../core/models/interfaces/map';
import {FormsModule} from '@angular/forms';
import {TurnPhase} from '../../../../core/enums/turn-phase';
import {NgForOf, NgIf} from '@angular/common';
import {CommonModule} from '@angular/common';
import {GamePlayService} from '../../services/game.play.service';
import {NotificationService} from '../../../../core/services/notification.service';

@Component({
  selector: 'app-fortify-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIf,
    NgForOf
  ],
  templateUrl: './fortify-form.component.html',
  styleUrls: ['./fortify-form.component.css']
})
export class FortifyFormComponent implements OnInit, OnChanges {
  @Input() isVisible: boolean = false;
  @Input() gameCode: string = '';
  @Input() playerId: number = 0;
  @Input({ transform: (value: TurnPhase | undefined): string | null => value ?? null })
  currentPhase!: string | null;

  @Output() closeModal = new EventEmitter<void>();
  @Output() fortificationCompleted = new EventEmitter<any>();

  fortifiableTerritories: any[] = [];
  fortificationTargets: any[] = [];
  
  selectedFrom: number | null = null;
  selectedTo: number | null = null;
  armiesToMove: number = 0;
  maxMovableArmies: number = 0;

  isLoading: boolean = false;

  constructor(
    private gamePlayService: GamePlayService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    if (this.isVisible && this.gameCode && this.playerId) {
      this.loadFortifiableTerritories();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isVisible'] && changes['isVisible'].currentValue && this.gameCode && this.playerId) {
      this.loadFortifiableTerritories();
    }
  }

  /**
   * Carga los territorios que pueden ser origen de fortificación
   */
  loadFortifiableTerritories() {
    if (!this.gameCode || !this.playerId) {
      return;
    }

    this.isLoading = true;
    this.gamePlayService.getFortifiableTerritories(this.gameCode, this.playerId).subscribe({
      next: (territories) => {
        this.fortifiableTerritories = territories;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error cargando territorios fortificables:', error);
        this.notificationService.showNotification(
          'error',
          'Error',
          'No se pudieron cargar los territorios fortificables.'
        );
        this.isLoading = false;
      }
    });
  }

  /**
   * Carga los territorios destino cuando se selecciona un origen
   */
  onFromChange() {
    if (!this.selectedFrom) {
      this.fortificationTargets = [];
      this.selectedTo = null;
      this.armiesToMove = 0;
      return;
    }

    this.isLoading = true;
    this.gamePlayService.getFortifyTargets(this.gameCode, this.selectedFrom, this.playerId).subscribe({
      next: (targets: any) => {
        this.fortificationTargets = targets;
        this.selectedTo = null;
        this.armiesToMove = 0;
        this.isLoading = false;
        console.log('Objetivos de fortificación cargados:', targets);
      },
      error: (error) => {
        console.error('Error cargando objetivos de fortificación:', error);
        this.notificationService.showNotification('error', 'Error', 'No se pudieron cargar los objetivos de fortificación.');
        this.isLoading = false;
      }
    });
  }

  /**
   * Ejecuta la fortificación
   */
  onSubmit() {
    if (!this.selectedFrom || !this.selectedTo || this.armiesToMove < 1) {
      this.notificationService.showNotification('warning', 'Datos incompletos', 'Por favor completa todos los campos.');
      return;
    }

    this.isLoading = true;

    const fortifyData = {
      playerId: this.playerId,
      fromCountryId: this.selectedFrom,
      toCountryId: this.selectedTo,
      armies: this.armiesToMove
    };

    this.gamePlayService.fortify(this.gameCode, fortifyData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.notificationService.showNotification(
          'success', 
          'Fortificación exitosa', 
          `Se movieron ${this.armiesToMove} ejércitos correctamente.`
        );
        
        // Emitir evento de completado
        this.fortificationCompleted.emit(response);
        
        // Cerrar modal
        this.closeModal.emit();
        
        // Resetear formulario
        this.resetForm();
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error en fortificación:', error);
        this.notificationService.showNotification(
          'error', 
          'Error en fortificación', 
          'No se pudo completar la fortificación. Verifica que los territorios estén conectados.'
        );
      }
    });
  }

  /**
   * Cierra el modal
   */
  onClose() {
    this.closeModal.emit();
    this.resetForm();
  }

  /**
   * Resetea el formulario
   */
  private resetForm() {
    this.selectedFrom = null;
    this.selectedTo = null;
    this.armiesToMove = 0;
    this.fortificationTargets = [];
  }

  /**
   * Verifica si el formulario es válido
   */
  isFormValid(): boolean {
    return !!(this.selectedFrom && this.selectedTo && this.armiesToMove > 0 && !this.isLoading);
  }
}
