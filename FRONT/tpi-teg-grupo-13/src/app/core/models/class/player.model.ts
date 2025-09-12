import { GamePlayer } from '../interfaces/game-player';
import { User } from '../interfaces/user';
import { Objective } from '../interfaces/objective';
import { Territory } from '../interfaces/map';
import { TerritoryCard } from '../interfaces/card';
import { PlayerState } from '../../enums/player-state';
import {GameState} from '../../enums/game-state';
import {BotLevel} from '../../enums/BotLevel';

export class PlayerModel implements GamePlayer {

  id: string;
  username: string | User;
  displayName: string | User;
  isBot: boolean;
  botLevel: BotLevel | null;
  armiesToPlace: number;
  seatOrder: number;
  color: string;



  status: PlayerState;
  territoryIds: number[];
  hand: TerritoryCard[];

  constructor(data: Partial<GamePlayer>) {
    // Obligatorios
    this.id            = data.id               || '';
    this.username      = data.username         || '';
    this.displayName   = data.displayName      || this.username;
    this.isBot         = data.isBot    ?? false;
    this.botLevel      = data.botLevel ?? null;
    this.armiesToPlace = data.armiesToPlace   ?? 0;
    this.seatOrder     = data.seatOrder        ?? 0;
    this.color         = (data as any).color   || this.randomColor();

    // Extra
    this.status       = (data as any).status       || PlayerState.WAITING;
    this.territoryIds = (data as any).territoryIds || [];
    this.hand         = (data as any).hand         || [];
  }

  private randomColor(): string {
    const paleta = ['#FF5733','#33FF57','#3357FF','#FF33F5','#F5FF33','#33FFF5'];
    return paleta[Math.floor(Math.random()*paleta.length)];
  }




  exchangeCards(cardIds: string[]): boolean {
    // TODO: Implement card exchange logic
    return true;
  }

  updateStatus(newStatus: PlayerState): void {
    this.status = newStatus;
  }
}
