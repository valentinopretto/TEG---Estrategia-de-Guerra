import { GameResult } from "../../enums/game-result";

// Interfaces for Game History
export interface GameHistory {
  id: string;
  date: Date;
  duration: number; // in minutes
  result: GameResult;
  players: string[];
  winner?: string;
  statistics: GameStatistics;
}

export interface GameStatistics {
  conqueredTerritories: number;
  troopsEliminated: number;
  controlledContinents: number;
  exchangedCards: number;
  attackImpactOnOtherCountries?: number;
}
