import {Component, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TerritoryCard } from '../../../core/models/interfaces/card'
import { CardTradeRequest } from '../../../core/models/interfaces/card-trade-request';


@Injectable({
  providedIn: 'root'
})
export class CardService {
  private baseUrl = 'http://localhost:8080/api/cards';

  constructor(private http: HttpClient) {}

  getPlayerCards(playerId: number): Observable<TerritoryCard[]> {
    return this.http.get<TerritoryCard[]>(`${this.baseUrl}/player/${playerId}`);
  }

  tradeCards(tradeRequest: CardTradeRequest): Observable<number> {
    return this.http.post<number>(`${this.baseUrl}/trade`, tradeRequest);
  }

  canPlayerTrade(playerId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/player/${playerId}/can-trade`);
  }

  mustPlayerTrade(playerId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/player/${playerId}/must-trade`);
  }

  getMaxCardsAllowed(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/max-cards-allowed`);
  }
}
