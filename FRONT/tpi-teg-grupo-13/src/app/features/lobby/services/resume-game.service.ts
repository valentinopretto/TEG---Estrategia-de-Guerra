import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

// Interfaces - se pueden mover a un archivo models.ts
export interface PlayerStatus {
  id: string;
  name: string;
  isReady: boolean;
  isConnected: boolean;
  isHost: boolean;
}

export interface GameLobby {
  gameId: string;
  hostId: string;
  players: PlayerStatus[];
  canResume: boolean;
  //TODO: Agregar los demas atributos que vienen del backend
}

@Injectable({
  providedIn: 'root'
})
export class ResumeGameService { 
  private apiUrl = `${environment.apiUrl}/games`; // TODO: ajustar la url

  constructor(private http: HttpClient) { }

  joinGameLobby(gameCode: string, playerId: string): Observable<GameLobby> {
    // TODO: Asumiendo que el backend espera playerId en el body o como un param
    // TODO: Ejemplo: POST /api/game/{gameCode}/join
    return this.http.post<GameLobby>(`${this.apiUrl}/${gameCode}/join`, { playerId: Number(playerId) });
  }

  togglePlayerReady(gameId: string, playerId: string): Observable<any> {
    // TODO: Ejemplo: PUT /api/game/{gameId}/player/{playerId}/ready
    return this.http.put(`${this.apiUrl}/${gameId}/player/${playerId}/ready`, {});
  }

  getGameLobbyStatus(gameId: string, playerId: string): Observable<GameLobby> {
    // TODO: Ejemplo: GET /api/game/{gameId}/status?playerId={playerId}
    // TODO: PlayerId might be needed if the status view is player-specific, or handled by auth token
    let params = new HttpParams();
    if (playerId) {
      params = params.set('playerId', playerId);
    }
    return this.http.get<GameLobby>(`${this.apiUrl}/${gameId}/status`, { params });
  }

  resumeGame(gameId: string): Observable<any> {
    // TODO: Ejemplo: POST /api/game/{gameId}/resume
    return this.http.post(`${this.apiUrl}/${gameId}/resume`, {});
  }

  disconnectFromLobby(gameId: string, playerId: string): Observable<any> {
    // TODO: Ejemplo: POST /api/game/{gameId}/player/{playerId}/disconnect or /leave
    return this.http.post(`${this.apiUrl}/${gameId}/player/${playerId}/disconnect`, {});
  }

  // TODO: hay que crear un juego? si es asi, que hace esta clase?
  // createGame(params: any): Observable<GameLobby> {
  //   return this.http.post<GameLobby>(`${this.apiUrl}/create`, params);
  // }
}
