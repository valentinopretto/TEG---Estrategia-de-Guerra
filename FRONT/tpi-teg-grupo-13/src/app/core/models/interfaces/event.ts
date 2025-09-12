import { EventType } from "../../enums/event-type";

export interface GameEvent {
  id: string;
  timestamp: Date;
  type: EventType;
  playerId: string;
  details: any;
  message: string;
}
