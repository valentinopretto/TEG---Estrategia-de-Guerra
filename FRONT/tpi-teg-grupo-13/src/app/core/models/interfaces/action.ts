import { ActionType } from "../../enums/action-type";

export interface GameAction {
  type: ActionType;
  from?: string; // Origin territory ID
  to?: string;   // Destination territory ID
  troops?: number;
}
