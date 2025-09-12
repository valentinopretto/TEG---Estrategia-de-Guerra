import { TurnPhase } from "../../enums/turn-phase";
import { GameAction } from "./action";

export interface GameTurn {
  playerId: string;
  phase: TurnPhase;
  timeRemaining: number;
  availableActions: GameAction[];
  troopsToPlace: number;
}
