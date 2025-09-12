import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { MessageListComponent } from '../message-list/message-list.component';
import { MessageInputComponent } from '../message-input/message-input.component';
import { ChatMessageResponseDto, ChatService, ChatMessageDto } from '../../services/chat.service';
import {AuthService} from '../../../../core/services/auth.service';
import {LobbyService} from '../../../lobby/services/lobby.service';
import Swal from 'sweetalert2';

interface ChatState {
  messages: ChatMessageResponseDto[],
  users: any[],
  isConnected: boolean,
  unreadCount: number
}

@Component({
  selector: 'app-chat-panel',
  standalone: true,
  imports: [CommonModule, MessageListComponent, MessageInputComponent],
  templateUrl: './chat-panel.component.html',
  styleUrls: ['./chat-panel.component.css']
})
export class ChatPanelComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Input() gameCode!: string;
  @ViewChild('chatContent') private chatContent!: ElementRef;

  chatState: ChatState = {
    messages: [],
    users: [],
    isConnected: false,
    unreadCount: 0
  };

  // Crear config simple
  config = {
    maxMessageLength: 150,
    showTimestamps: true,
    showAvatars: true,
    autoScroll: true
  };

  userId: number = 0; //ID DEL USUARIO NO COINCIDE NUNCA CON EL PLAYER ID, error descomunal
  userName: string = '';
  gameId: number = 0;

  // ✨ Variables para el auto-scroll
  private shouldScrollToBottom = true;
  private lastMessageCount = 0;

  private chatSubscription?: Subscription;

  constructor(private chatService: ChatService,
              private authService: AuthService,
              private lobbyService: LobbyService) {}

  ngOnInit() {
    if (this.gameCode) {
      this.initializeChat();
    }
  }

  ngOnDestroy() {
    this.cleanupChat();
  }

  // ✨ Auto-scroll después de cada cambio en la vista
  ngAfterViewChecked() {
    this.scrollToBottomIfNeeded();
  }

  private initializeChat() {
    // Suscribirse a los mensajes
    this.chatSubscription = this.chatService.messages$.subscribe(
      (messages: ChatMessageResponseDto[]) => {
        // Convertir timestamps a Date si vienen como string
        this.chatState.messages = messages.map(msg => ({
          ...msg,
          timestamp: new Date(msg.sentAt)
        }));
        this.chatState.isConnected = true;

        // ✨ Activar scroll cuando llegan nuevos mensajes
        this.shouldScrollToBottom = true;
      }
    );

    // Iniciar polling
    this.chatService.startChatPolling(this.gameCode);
  }

  private cleanupChat() {
    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
    }
    this.chatService.stopChatPolling();
  }

  onSendMessage(messageText: string) {
    if (!messageText.trim() || !this.gameCode) return;

    this.userName = this.authService.getCurrentUser()?.username ?? '';
    this.userId = this.authService.getCurrentUser()?.id ?? 0;

    console.log('🔍 Debug usuario actual:', {
      userId: this.userId,
      userName: this.userName,
      gameCode: this.gameCode
    });

    if (this.userId === 0) {
      Swal.fire({
        icon: 'error',
        title: 'Error de autenticación',
        text: 'Debes estar autenticado para acceder a esta página.'
      });
      return;
    }

    this.lobbyService.getGameByCode(this.gameCode).subscribe({
      next: (game) => {
        if (!game) {
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo encontrar el juego.'
          });
          return;
        }

        const isPlayerInGame = game.players.some(player => {
          const playerName = player.name || player.username || player.playerName || player.user?.name || player.user?.username;
          return playerName === this.userName;
        });

        if (!isPlayerInGame) {
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: `No estás en este juego. Tu nombre: "${this.userName}"`
          });
          return;
        }

        // Buscar el ID del jugador basado en el nombre de usuario
        const player = game.players.find(player => {
          const playerName = player.name || player.username || player.playerName || player.user?.name || player.user?.username;
          return playerName === this.userName;
        });

        const playerId = player?.id;

        const messageDto: ChatMessageDto = {
          senderId: playerId, //tiene que ser el id del player, no del usuario
          gameId: game.id,
          content: messageText.trim()
        };
        console.log('📤 Enviando mensaje:', messageDto);
        this.chatService.sendMessage(this.gameCode, messageDto).subscribe({
          next: (response: ChatMessageResponseDto) => {
            console.log('✅ Mensaje enviado exitosamente:', response);
            // ✨ Forzar scroll después de enviar
            this.shouldScrollToBottom = true;

            // ✨ Debug la respuesta para verificar la fecha
            console.log('📅 Respuesta con timestamp:', {
              ...response,
              sentAtType: typeof response.sentAt
            });
          },
          error: (error: Error) => {
            console.error('❌ Error al enviar mensaje:', error);
            Swal.fire({
              icon: 'error',
              title: 'Error al enviar mensaje',
              text: 'No se pudo enviar el mensaje. Inténtalo de nuevo.'
            });
          }
        });
      },
      error: (error) => {
        console.error('❌ Error al obtener el juego:', error);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo verificar el acceso al juego.'
        });
      }
    });
  }

  onFocusChat() {
    this.chatState.unreadCount = 0;
    // ✨ Scroll al enfocar el chat
    this.scrollToBottomManually();
  }

  // ✨ Métodos para manejar el auto-scroll
  private scrollToBottomIfNeeded(): void {
    try {
      // Solo hacer scroll si hay nuevos mensajes o se debe forzar
      if (this.shouldScrollToBottom || this.hasNewMessages()) {
        this.scrollToBottom();
        this.shouldScrollToBottom = false;
        this.lastMessageCount = this.chatState.messages.length;
      }
    } catch (err) {
      console.warn('Error al hacer scroll en el chat:', err);
    }
  }

  private hasNewMessages(): boolean {
    return this.chatState.messages.length > this.lastMessageCount;
  }

  private scrollToBottom(): void {
    if (this.chatContent?.nativeElement) {
      const element = this.chatContent.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  // ✨ Método público para scroll manual
  scrollToBottomManually(): void {
    this.shouldScrollToBottom = true;
    setTimeout(() => this.scrollToBottom(), 100);
  }

  // ✨ Detectar si el usuario está viendo mensajes antiguos
  onChatScroll(event: Event): void {
    const element = event.target as HTMLElement;
    const tolerance = 50; // pixels de tolerancia

    // Si el usuario está cerca del final, auto-scroll estará activo
    const isNearBottom = element.scrollTop + element.clientHeight >= element.scrollHeight - tolerance;
    this.shouldScrollToBottom = isNearBottom;
  }
}
