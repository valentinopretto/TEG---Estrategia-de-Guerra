import { Component, Input, Output, EventEmitter, OnChanges, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatMessageResponseDto } from '../../services/chat.service';

@Component({
  selector: 'app-message-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-list.component.html',
  styleUrls: ['./message-list.component.css']
})
export class MessageListComponent implements OnChanges, AfterViewChecked {
  @Input() messages: ChatMessageResponseDto[] = [];
  @Input() config: any = {};
  @Output() onFocus = new EventEmitter<void>();

  @ViewChild('messagesContainer', { static: false }) messagesContainer!: ElementRef;

  private shouldScrollToBottom = true;

  ngOnChanges() {
    // ✨ Debug para ver qué mensajes llegan
    console.log('📨 Mensajes recibidos:', this.messages.map(m => ({
      content: m.content?.substring(0, 20) + '...',
      sentAt: m.sentAt,
      sentAtType: typeof m.sentAt
    })));

    this.shouldScrollToBottom = true;
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        const element = this.messagesContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      }
    } catch (err) {
      console.error('Error al hacer scroll:', err);
    }
  }

  onContainerFocus() {
    this.onFocus.emit();
  }

  // ✨ MÉTODO CORREGIDO para formatear fechas de LocalDateTime de Java
  formatTime(timestamp: any): string {
    try {
      let date: Date;

      // Manejar LocalDateTime como array [year, month, day, hour, minute, second, nano]
      if (Array.isArray(timestamp) && timestamp.length >= 3) {
        const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = timestamp;
        date = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1000000));
      }
      // Manejar LocalDateTime como objeto
      else if (timestamp && typeof timestamp === 'object' && !Array.isArray(timestamp)) {
        if (timestamp.year && timestamp.month && timestamp.day) {
          date = new Date(
            timestamp.year,
            timestamp.month - 1,
            timestamp.day,
            timestamp.hour || 0,
            timestamp.minute || 0,
            timestamp.second || 0,
            Math.floor((timestamp.nano || 0) / 1000000)
          );
        } else {
          console.warn('⚠️ Objeto timestamp sin estructura esperada:', timestamp);
          return 'Hora inválida';
        }
      }
      // Manejar string ISO
      else if (typeof timestamp === 'string') {
        date = new Date(timestamp);
      }
      // Manejar timestamp numérico
      else if (typeof timestamp === 'number') {
        date = new Date(timestamp);
      }
      // Manejar Date object
      else if (timestamp instanceof Date) {
        date = timestamp;
      }
      else {
        console.warn('⚠️ Timestamp inválido:', timestamp);
        return 'Hora inválida';
      }

      // Validar que la fecha es válida
      if (isNaN(date.getTime())) {
        console.warn('⚠️ Fecha inválida generada:', date, 'desde:', timestamp);
        return 'Hora inválida';
      }

      // Formatear la fecha
      return date.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      });

    } catch (error) {
      console.error('❌ Error al formatear timestamp:', error, timestamp);
      return 'Hora inválida';
    }
  }

  isSystemMessage(message: ChatMessageResponseDto): boolean {
    return message.senderId === 'system' || message.senderName === 'Sistema';
  }

  isPrivateMessage(message: ChatMessageResponseDto): boolean {
    return message.isPrivate || false;
  }

  trackByMessageId(index: number, message: ChatMessageResponseDto): string {
    // ✨ Usar index como fallback si no hay ID
    return message.id || `msg-${index}`;
  }

  // ✨ MÉTODO ADICIONAL para debug
  debugMessage(message: ChatMessageResponseDto): void {
    console.log('🔍 Debug mensaje:', {
      id: message.id,
      content: message.content,
      sentAt: message.sentAt,
      senderName: message.senderName,
      senderId: message.senderId
    });
  }
}
