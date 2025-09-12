import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, interval, switchMap, takeWhile, catchError, of } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';

export interface ChatMessageDto {
  senderId: number;
  gameId: number;
  content: string;
  recipientId?: string;
}

export interface ChatMessageResponseDto {
  id: string;
  senderId: string;
  senderName: string;
  content: string;
  sentAt: Date;
  recipientId?: string;
  isSystemMessage: boolean;
  isPrivate: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly API_URL = `${environment.apiUrl}/games`;
  private messagesSubject = new BehaviorSubject<ChatMessageResponseDto[]>([]);
  private isPollingActive = false;

  public messages$ = this.messagesSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Enviar mensaje de chat
   */
  sendMessage(gameCode: string, message: ChatMessageDto): Observable<ChatMessageResponseDto> {
    //const options = this.authService.getRequestOptions();
    return this.http.post<ChatMessageResponseDto>(`${this.API_URL}/${gameCode}/chat`, message);
  }

  /**
   * Obtener mensajes nuevos
   */
  getNewMessages(gameCode: string, sinceMessageId?: string): Observable<ChatMessageResponseDto[]> {
    //const options = this.authService.getRequestOptions();
    let url = `${this.API_URL}/${gameCode}/chat/new`;

    if (sinceMessageId) {
      url += `?since=${sinceMessageId}`;
    }

    return this.http.get<ChatMessageResponseDto[]>(url);
  }

  /**
   * Iniciar polling para mensajes
   */
  startChatPolling(gameCode: string): void {
    if (this.isPollingActive) return;

    this.isPollingActive = true;
    let lastMessageId: string | null = null;

    interval(2000).pipe( // Polling cada 2 segundos
      takeWhile(() => this.isPollingActive),
      switchMap(() => this.getNewMessages(gameCode, lastMessageId || undefined)),
      catchError(error => {
        console.error('Error en polling de chat:', error);
        return of([]);
      })
    ).subscribe(newMessages => {
      if (newMessages.length > 0) {
        const currentMessages = this.messagesSubject.value;
        const updatedMessages = [...currentMessages, ...newMessages];
        this.messagesSubject.next(updatedMessages);
        lastMessageId = newMessages[newMessages.length - 1].id;
      }
    });
  }

  /**
   * Detener polling
   */
  stopChatPolling(): void {
    this.isPollingActive = false;
  }

  /**
   * Limpiar mensajes
   */
  clearMessages(): void {
    this.messagesSubject.next([]);
  }
}
