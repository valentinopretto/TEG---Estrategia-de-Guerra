import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import Swal, { SweetAlertResult } from 'sweetalert2'

export interface NotificationConfig {
  duration?: number;
  position?: 'top' | 'bottom' | 'center';
  showConfirmButton?: boolean;
  allowOutsideClick?: boolean;
}

export interface GameNotification {
  id: string;
  type: 'info' | 'success' | 'warning' | 'error' | 'game-event';
  title: string;
  message: string;
  timestamp: Date;
  gameId?: string;
  playerId?: string;
  actionRequired?: boolean;
  data?: any;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<GameNotification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private maxNotifications = 50;
  private notificationIdCounter = 0;

  constructor() {}

  /**
   * Mostrar notificación general
   */
  showNotification(
    type: 'info' | 'success' | 'warning' | 'error',
    title: string,
    message: string,
    config?: NotificationConfig
  ): void {
    const defaultConfig: NotificationConfig = {
      duration: 5000,
      position: 'top',
      showConfirmButton: false,
      allowOutsideClick: true
    };

    const finalConfig = { ...defaultConfig, ...config };

    // Mostrar notificación con SweetAlert2
    Swal.fire({
      icon: type,
      title: title,
      text: message,
      position: finalConfig.position,
      timer: finalConfig.duration,
      showConfirmButton: finalConfig.showConfirmButton,
      toast: true,
      timerProgressBar: true
    });

    // Agregar a la lista de notificaciones
    this.addToNotificationsList(type, title, message);
  }

  /**
   * Mostrar notificación de juego específica
   */
  showGameNotification(
    title: string,
    message: string,
    gameId?: string,
    playerId?: string,
    actionRequired: boolean = false,
    data?: any
  ): void {
    const notification: GameNotification = {
      id: this.generateId(),
      type: 'game-event',
      title,
      message,
      timestamp: new Date(),
      gameId,
      playerId,
      actionRequired,
      data
    };

    this.addNotification(notification);

    // Mostrar también como notificación visual si requiere acción
    if (actionRequired) {
      Swal.fire({
        icon: 'info',
        title: title,
        text: message,
        showConfirmButton: true,
        confirmButtonText: 'Entendido'
      });
    } else {
      this.showNotification('info', title, message);
    }
  }

  /**
   * Notificaciones específicas del TEG
   */

  // Notificaciones de turno
  notifyTurnStart(playerName: string): void {
    this.showGameNotification(
      'Nuevo Turno',
      `Es el turno de ${playerName}`,
      undefined,
      undefined,
      false
    );
  }

  notifyYourTurn(): void {
    this.showGameNotification(
      '¡Tu Turno!',
      'Es tu turno para jugar',
      undefined,
      undefined,
      true
    );
  }

  // Notificaciones de combate
  notifyAttackResult(
    attackerName: string,
    defenderName: string,
    fromCountry: string,
    toCountry: string,
    conquered: boolean
  ): void {
    const message = conquered
      ? `${attackerName} conquistó ${toCountry} desde ${fromCountry}`
      : `${attackerName} atacó ${toCountry} desde ${fromCountry} sin éxito`;

    this.showGameNotification(
      'Resultado de Ataque',
      message,
      undefined,
      undefined,
      false
    );
  }

  notifyUnderAttack(attackerName: string, countryName: string): void {
    this.showGameNotification(
      '¡Bajo Ataque!',
      `${attackerName} está atacando tu territorio ${countryName}`,
      undefined,
      undefined,
      true
    );
  }

  // Notificaciones de eliminación
  notifyPlayerEliminated(playerName: string): void {
    this.showGameNotification(
      'Jugador Eliminado',
      `${playerName} ha sido eliminado del juego`,
      undefined,
      undefined,
      false
    );
  }

  notifyYouAreEliminated(): void {
    this.showGameNotification(
      'Has sido eliminado',
      'Has perdido todos tus territorios y has sido eliminado del juego',
      undefined,
      undefined,
      true
    );
  }

  // Notificaciones de victoria
  notifyGameWon(winnerName: string): void {
    this.showGameNotification(
      '¡Fin del Juego!',
      `${winnerName} ha ganado la partida`,
      undefined,
      undefined,
      true
    );
  }

  notifyYouWon(): void {
    Swal.fire({
      icon: 'success',
      title: '¡Felicitaciones!',
      text: '¡Has ganado la partida!',
      showConfirmButton: true,
      confirmButtonText: 'Genial!'
    });
  }

  // Notificaciones de cartas
  notifyCardReceived(): void {
    this.showNotification('success', 'Carta Recibida', 'Has recibido una nueva carta de país');
  }

  notifyCardsTraded(armies: number): void {
    this.showNotification('success', 'Cartas Intercambiadas', `Has recibido ${armies} ejércitos por el intercambio`);
  }

  // Notificaciones de conexión
  notifyPlayerJoined(playerName: string): void {
    this.showGameNotification(
      'Jugador Unido',
      `${playerName} se ha unido a la partida`,
      undefined,
      undefined,
      false
    );
  }

  notifyPlayerLeft(playerName: string): void {
    this.showGameNotification(
      'Jugador Desconectado',
      `${playerName} ha abandonado la partida`,
      undefined,
      undefined,
      false
    );
  }

  notifyGameStarted(): void {
    this.showGameNotification(
      '¡Partida Iniciada!',
      'La partida ha comenzado. ¡Buena suerte!',
      undefined,
      undefined,
      true
    );
  }

  /**
   * Confirmaciones y diálogos
   */

  confirmAction(title: string, message: string, confirmText: string = 'Confirmar'): Promise<boolean> {
    return Swal.fire({
      icon: 'question',
      title: title,
      text: message,
      showCancelButton: true,
      confirmButtonText: confirmText,
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33'
    }).then((result: SweetAlertResult) => {
      return result.isConfirmed;
    });
  }

  confirmLeaveGame(): Promise<boolean> {
    return this.confirmAction(
      '¿Abandonar Partida?',
      'Si abandonas la partida serás eliminado automáticamente. ¿Estás seguro?',
      'Sí, abandonar'
    );
  }

  confirmEndTurn(): Promise<boolean> {
    return this.confirmAction(
      '¿Terminar Turno?',
      '¿Estás seguro que quieres terminar tu turno?',
      'Terminar Turno'
    );
  }

  confirmAttack(fromCountry: string, toCountry: string): Promise<boolean> {
    return this.confirmAction(
      'Confirmar Ataque',
      `¿Atacar ${toCountry} desde ${fromCountry}?`,
      'Atacar'
    );
  }

  /**
   * Métodos auxiliares
   */

  private addToNotificationsList(
    type: 'info' | 'success' | 'warning' | 'error',
    title: string,
    message: string
  ): void {
    const notification: GameNotification = {
      id: this.generateId(),
      type,
      title,
      message,
      timestamp: new Date()
    };

    this.addNotification(notification);
  }

  private addNotification(notification: GameNotification): void {
    const notifications = this.notificationsSubject.value;
    const updatedNotifications = [notification, ...notifications];

    // Mantener solo las últimas N notificaciones
    if (updatedNotifications.length > this.maxNotifications) {
      updatedNotifications.splice(this.maxNotifications);
    }

    this.notificationsSubject.next(updatedNotifications);
  }

  private generateId(): string {
    return `notification_${Date.now()}_${++this.notificationIdCounter}`;
  }

  /**
   * Gestión de notificaciones
   */

  getNotifications(): GameNotification[] {
    return this.notificationsSubject.value;
  }

  getGameNotifications(gameId: string): GameNotification[] {
    return this.notificationsSubject.value.filter(n => n.gameId === gameId);
  }

  markAsRead(notificationId: string): void {
    const notifications = this.notificationsSubject.value;
    const notification = notifications.find(n => n.id === notificationId);
    if (notification) {
      notification.actionRequired = false;
    }
    this.notificationsSubject.next([...notifications]);
  }

  clearNotifications(): void {
    this.notificationsSubject.next([]);
  }

  clearGameNotifications(gameId: string): void {
    const notifications = this.notificationsSubject.value;
    const filtered = notifications.filter(n => n.gameId !== gameId);
    this.notificationsSubject.next(filtered);
  }

  /**
   * Errores específicos del juego
   */

  showError(message: string, title: string = 'Error'): void {
    this.showNotification('error', title, message, {
      duration: 8000,
      showConfirmButton: true
    });
  }

  showValidationError(field: string, message: string): void {
    this.showError(`${field}: ${message}`, 'Error de Validación');
  }

  showConnectionError(): void {
    this.showError(
      'No se pudo conectar con el servidor. Verifica tu conexión a internet.',
      'Error de Conexión'
    );
  }

  showGameActionError(action: string, reason: string): void {
    this.showError(
      `No se pudo realizar la acción "${action}": ${reason}`,
      'Acción No Válida'
    );
  }
}
