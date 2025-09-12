// src/app/features/join-game/services/game.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService, SimpleUser } from '../../../core/services/auth.service';

export interface SavedGame {
  id: string;
  fechaCreacion: Date;
  fechaUltimaJugada: Date;
  estado: 'en_progreso' | 'finalizada';
  jugadores: string[];
  turnoActual: number;
}

export interface UnirseResponse { partidaId: string; mensaje: string; }
export interface ContinuarResponse { partidaId: string; estado: any; mensaje: string; }
export interface ResumeGame{partidaId: string}

@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly API_URL = `${environment.apiUrl}/games`;

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) {}

  /**
   * Obtiene las partidas donde el usuario es host,
   * adapta fecha y lista de jugadores al modelo SavedGame.
   */
  obtenerPartidasDelHostAdaptadas(): Observable<SavedGame[]> {
    const user: SimpleUser | null = this.auth.getCurrentUser();
    if (!user) {
      throw new Error('Usuario no autenticado');
    }

    return this.http
      .get<any[]>(`${this.API_URL}/host/${user.id}`)
      .pipe(
        map((resp: any[]) =>
          resp.map(raw => this.adaptarPartida(raw))
        )
      );
  }

  unirseConCodigo(codigo: string): Observable<UnirseResponse> {
    const user: SimpleUser | null = this.auth.getCurrentUser();
    if (!user) throw new Error('Usuario no autenticado');
    return this.http.post<UnirseResponse>(
      `${this.API_URL}/join`,
      { gameCode: codigo, userId: user.id }
    );
  }

  continuarPartida(partidaId: string): Observable<ResumeGame> {
    return this.http.post<ResumeGame>(
      `${this.API_URL}/${partidaId}/resume`,
      {}
    );
  }

  eliminarPartida(partidaId: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${partidaId}`);
  }


  private adaptarPartida(data: any): SavedGame {
    // createdAt y startedAt vienen como array de números
    const createdAtArray = data.createdAt as number[];
    const startedAtArray = Array.isArray(data.startedAt)
      ? (data.startedAt as number[])
      : null;

    return {
      id: String(data.id),
      fechaCreacion: this.parseFechaArray(createdAtArray),
      fechaUltimaJugada: startedAtArray
        ? this.parseFechaArray(startedAtArray)
        : this.parseFechaArray(createdAtArray),
      estado: data.finishedAt ? 'finalizada' : 'en_progreso',
      jugadores: (data.players ?? [])
        .map((p: any) => p.displayName ?? p.username ?? 'Jugador'),
      turnoActual: data.currentTurn ?? 0
    };
  }

  //Convierte un array [YYYY, M, D, h, m, s, ns] a Date de JS.
   // Restamos 1 al mes (JS usa 0–11) y convertimos ns a ms.

  private parseFechaArray(fecha: number[]): Date {
    return new Date(
      fecha[0],               // año
      fecha[1] - 1,           // mes JS
      fecha[2],               // día
      fecha[3],               // hora
      fecha[4],               // minutos
      fecha[5],               // segundos
      Math.floor((fecha[6] || 0) / 1_000_000) // nanosegundos → ms
    );
  }
}
