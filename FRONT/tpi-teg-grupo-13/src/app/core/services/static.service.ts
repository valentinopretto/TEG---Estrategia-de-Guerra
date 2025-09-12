// import { Injectable } from '@angular/core';
// import { HttpClient } from '@angular/common/http';
// import { Observable, BehaviorSubject } from 'rxjs';
// import { environment } from '../../../environments/environment';
// import { AuthService } from './auth.service';
//
// export interface PlayerStatistics {
//   playerId: number;
//   playerName: string;
//   attacksPerformed: number;
//   conquests: number;
//   defenses: number;
//   attackSuccessRate: number;
//   defenseSuccessRate: number;
//   territoriesLost: number;
//   territoriesGained: number;
//   armiesPlaced: number;
//   armiesLost: number;
//   cardsTraded: number;
//   continentsControlled: number;
//   longestAttackStreak: number;
//   longestDefenseStreak: number;
// }
//
// export interface GameStatistics {
//   gameId: number;
//   totalTurns: number;
//   gameDuration: string; // ISO duration format
//   totalAttacks: number;
//   territoriesConquered: number;
//   mostAggressivePlayer: string;
//   mostDefensivePlayer: string;
//   playerStats: PlayerStatistics[];
//   territoriesChangedHands: number;
//   averageTurnDuration: number;
//   longestTurn: number;
//   shortestTurn: number;
// }
//
// export interface GlobalStatistics {
//   totalUsers: number;
//   totalGames: number;
//   activeGames: number;
//   averageGameDuration: number;
//   gamesThisWeek: number;
//   gamesThisMonth: number;
//   mostPopularGameMode: string;
//   peakConcurrentPlayers: number;
//   gamesByStatus: {
//     waiting: number;
//     inProgress: number;
//     finished: number;
//     cancelled: number;
//   };
// }
//
// export interface LeaderboardEntry {
//   rank: number;
//   username: string;
//   wins: number;
//   losses: number;
//   winRate: number;
//   totalGames: number;
//   averageGameDuration: number;
//   favoriteStrategy: string;
//   lastActive: Date;
// }
//
// export interface TrendData {
//   date: string;
//   gamesPlayed: number;
//   newUsers: number;
//   activeUsers: number;
// }
//
// @Injectable({
//   providedIn: 'root'
// })
// export class StatisticsService {
//   private readonly API_URL = `${environment.apiUrl}/statistics`;
//
//   // Subjects para diferentes tipos de estadísticas
//   private globalStatsSubject = new BehaviorSubject<GlobalStatistics | null>(null);
//   public globalStats$ = this.globalStatsSubject.asObservable();
//
//   private leaderboardSubject = new BehaviorSubject<LeaderboardEntry[]>([]);
//   public leaderboard$ = this.leaderboardSubject.asObservable();
//
//   constructor(
//     private http: HttpClient,
//     private authService: AuthService
//   ) {}
//
//   /**
//    * Estadísticas globales del sistema
//    */
//   getGlobalStatistics(): Observable<GlobalStatistics> {
//     return this.http.get<GlobalStatistics>(`${this.API_URL}/global`);
//   }
//
//   /**
//    * Estadísticas de un juego específico
//    */
//   getGameStatistics(gameId: number): Observable<GameStatistics> {
//     const options = this.authService.getRequestOptions();
//     return this.http.get<GameStatistics>(`${this.API_URL}/game/${gameId}`, options);
//   }
//
//   /**
//    * Estadísticas de un jugador en un juego específico
//    */
//   getPlayerGameStatistics(playerId: number, gameId: number): Observable<PlayerStatistics> {
//     const options = this.authService.getRequestOptions();
//     return this.http.get<PlayerStatistics>(`${this.API_URL}/player/${playerId}/game/${gameId}`, options);
//   }
//
//   /**
//    * Estadísticas generales de un jugador
//    */
//   getPlayerOverallStatistics(playerId: number): Observable<any> {
//     const options = this.authService.getRequestOptions();
//     return this.http.get(`${this.API_URL}/player/${playerId}/overall`, options);
//   }
//
//   /**
//    * Tabla de posiciones/ranking
//    */
//   getLeaderboard(limit: number = 100, sortBy: string = 'winRate'): Observable<LeaderboardEntry[]> {
//     const params = {
//       limit: limit.toString(),
//       sortBy
//     };
//     return this.http.get<LeaderboardEntry[]>(`${this.API_URL}/leaderboard`, { params });
//   }
//
//   /**
//    * Ranking por categoría específica
//    */
//   getLeaderboardByCategory(category: 'wins' | 'winRate' | 'totalGames' | 'averageDuration', limit: number = 50): Observable<LeaderboardEntry[]> {
//     const params = {
//       category,
//       limit: limit.toString()
//     };
//     return this.http.get<LeaderboardEntry[]>(`${this.API_URL}/leaderboard/category`, { params });
//   }
//
//   /**
//    * Tendencias históricas
//    */
//   getTrends(period: 'week' | 'month' | 'year'): Observable<TrendData[]> {
//     const params = { period };
//     return this.http.get<TrendData[]>(`${this.API_URL}/trends`, { params });
//   }
//
//   /**
//    * Estadísticas del usuario actual
//    */
//   getCurrentUserStatistics(): Observable<any> {
//     const options = this.authService.getRequestOptions();
//     return this.http.get(`${this.API_URL}/user/current`, options);
//   }
//
//   /**
//    * Comparar estadísticas entre jugadores
//    */
//   comparePlayerStatistics(playerIds: number[]): Observable<any> {
//     const options = this.authService.getRequestOptions();
//     const body = { playerIds };
//     return this.http.post(`${this.API_URL}/compare`, body, options);
//   }
//
//   /**
//    * Estadísticas por estrategia de juego
//    */
//   getStrategyStatistics(): Observable<any> {
//     return this.http.get(`${this.API_URL}/strategies`);
//   }
//
//   /**
//    * Estadísticas de territorios más disputados
//    */
//   getTerritoryStatistics(): Observable<any> {
//     return this.http.get(`${this.API_URL}/territories`);
//   }
//
//   /**
//    * Estadísticas de continentes más valiosos
//    */
//   getContinentStatistics(): Observable<any> {
//     return this.http.get(`${this.API_URL}/continents`);
//   }
//
//   /**
//    * Estadísticas de tiempo de juego
//    */
//   getTimeStatistics(userId?: number): Observable<any> {
//     const options = this.authService.getRequestOptions();
//     const url = userId
//       ? `${this.API_URL}/time/user/${userId}`
//       : `${this.API_URL}/time/current`;
//     return this.http.get(url, options);
//   }
//
//   /**
//    * Progreso del jugador a lo largo del tiempo
//    */
//   getPlayerProgress(playerId: number, period: 'month' | 'quarter' | 'year'): Observable<any> {
//     const options = this.authService.getRequestOptions();
//     const params = { period };
//     return this.http.get(`${this.API_URL}/player/${playerId}/progress`, { ...options, params });
//   }
//
//   /**
//    * Logros y hitos del jugador
//    */
//   getPlayerAchievements(playerId?: number): Observable<any> {
//     const options = this.authService.getRequestOptions();
//     const url = playerId
//       ? `${this.API_URL}/player/${playerId}/achievements`
//       : `${this.API_URL}/user/achievements`;
//     return this.http.get(url, options);
//   }
//
//   /**
//    * Estadísticas de partidas por día de la semana/hora
//    */
//   getPlayTimeDistribution(): Observable<any> {
//     return this.http.get(`${this.API_URL}/playtime-distribution`);
//   }
//
//   /**
//    * Métodos de utilidad y cálculo local
//    */
//
//   /**
//    * Calcular porcentaje de victoria
//    */
//   calculateWinRate(wins: number, totalGames: number): number {
//     if (totalGames === 0) return 0;
//     return Math.round((wins / totalGames) * 100 * 100) / 100; // 2 decimales
//   }
//
//   /**
//    * Calcular promedio de duración en formato legible
//    */
//   formatDuration(minutes: number): string {
//     if (minutes < 60) {
//       return `${minutes} min`;
//     }
//     const hours = Math.floor(minutes / 60);
//     const remainingMinutes = minutes % 60;
//     return `${hours}h ${remainingMinutes}m`;
//   }
//
//   /**
//    * Obtener rango basado en estadísticas
//    */
//   getPlayerRank(winRate: number, totalGames: number): string {
//     if (totalGames < 5) return 'Principiante';
//     if (winRate >= 80 && totalGames >= 50) return 'Legendario';
//     if (winRate >= 70 && totalGames >= 30) return 'Maestro';
//     if (winRate >= 60 && totalGames >= 20) return 'Experto';
//     if (winRate >= 50 && totalGames >= 10) return 'Avanzado';
//     if (winRate >= 40) return 'Intermedio';
//     return 'Principiante';
//   }
//
//   /**
//    * Comparar rendimiento con promedio global
//    */
//   compareWithGlobal(userStats: any, globalAverage: any): any {
//     return {
//       winRate: {
//         user: userStats.winRate,
//         global: globalAverage.winRate,
//         difference: userStats.winRate - globalAverage.winRate,
//         better: userStats.winRate > globalAverage.winRate
//       },
//       averageGameDuration: {
//         user: userStats.averageGameDuration,
//         global: globalAverage.averageGameDuration,
//         difference: userStats.averageGameDuration - globalAverage.averageGameDuration,
//         faster: userStats.averageGameDuration < globalAverage.averageGameDuration
//       },
//       attackSuccessRate: {
//         user: userStats.attackSuccessRate,
//         global: globalAverage.attackSuccessRate,
//         difference: userStats.attackSuccessRate - globalAverage.attackSuccessRate,
//         better: userStats.attackSuccessRate > globalAverage.attackSuccessRate
//       }
//     };
//   }
//
//   /**
//    * Cargar estadísticas globales y almacenarlas
//    */
//   loadGlobalStatistics(): void {
//     this.getGlobalStatistics().subscribe({
//       next: (stats) => {
//         this.globalStatsSubject.next(stats);
//       },
//       error: (error) => {
//         console.error('Error loading global statistics:', error);
//       }
//     });
//   }
//
//   /**
//    * Cargar tabla de posiciones
//    */
//   loadLeaderboard(limit: number = 100): void {
//     this.getLeaderboard(limit).subscribe({
//       next: (leaderboard) => {
//         this.leaderboardSubject.next(leaderboard);
//       },
//       error: (error) => {
//         console.error('Error loading leaderboard:', error);
//       }
//     });
//   }
//
//   /**
//    * Obtener posición del usuario en el ranking
//    */
//   getUserRanking(userId: number): Observable<{ rank: number; total: number }> {
//     const options = this.authService.getRequestOptions();
//     return this.http.get<{ rank: number; total: number }>(`${this.API_URL}/user/${userId}/ranking`, options);
//   }
//
//   /**
//    * Limpiar datos del servicio
//    */
//   clearStatistics(): void {
//     this.globalStatsSubject.next(null);
//     this.leaderboardSubject.next([]);
//   }
// }
