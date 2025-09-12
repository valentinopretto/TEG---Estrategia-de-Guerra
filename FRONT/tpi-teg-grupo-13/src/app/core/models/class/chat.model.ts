export interface ChatMessage {
  id: string;
  gameId: string;
  senderId: string;
  senderName: string;
  text: string;
  timestamp: Date;
  isSystemMessage: boolean;
  recipientId?: string; // Para mensajes privados
}

export interface ChatMessageRequest {
  text: string;
  recipientId?: string;
}

// Clase para manejar mensajes de chat
export class ChatMessageModel {
  constructor(
    public id: string = '',
    public gameId: string = '',
    public senderId: string = '',
    public senderName: string = '',
    public text: string = '',
    public timestamp: Date = new Date(),
    public isSystemMessage: boolean = false,
    public recipientId?: string
  ) {}

  // Verificar si es mensaje privado
  isPrivateMessage(): boolean {
    return this.recipientId !== undefined && this.recipientId !== null;
  }

  // Verificar si es mensaje del sistema
  isFromSystem(): boolean {
    return this.isSystemMessage;
  }

  // Obtener tiempo transcurrido
  getTimeAgo(): string {
    const now = new Date();
    const diffMs = now.getTime() - this.timestamp.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'ahora';
    if (diffMins < 60) return `${diffMins}m`;
    if (diffHours < 24) return `${diffHours}h`;
    return `${diffDays}d`;
  }

  // Formatear tiempo completo
  getFormattedTime(): string {
    return this.timestamp.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // Crear desde datos del backend
  static fromApiData(data: any): ChatMessageModel {
    return new ChatMessageModel(
      data.id,
      data.gameId,
      data.senderId,
      data.senderName,
      data.text || data.content || '',
      new Date(data.timestamp || data.sentAt),
      data.isSystemMessage || false,
      data.recipientId
    );
  }

  // Crear mensaje del sistema
  static createSystemMessage(gameId: string, text: string): ChatMessageModel {
    return new ChatMessageModel(
      Date.now().toString(),
      gameId,
      'system',
      'Sistema',
      text,
      new Date(),
      true
    );
  }

  // Crear mensaje de jugador
  static createPlayerMessage(
    gameId: string,
    senderId: string,
    senderName: string,
    text: string,
    recipientId?: string
  ): ChatMessageModel {
    return new ChatMessageModel(
      Date.now().toString(),
      gameId,
      senderId,
      senderName,
      text,
      new Date(),
      false,
      recipientId
    );
  }
}