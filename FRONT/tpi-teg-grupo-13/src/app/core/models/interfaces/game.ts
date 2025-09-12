import { GameState } from "../../enums/game-state";
import { SpecialRule } from "../../enums/special-rule";
import { ChatMessage } from "./chat";
import { GameEvent } from "./event";
import { GamePlayer } from "./game-player";
import { GameMap } from "./map";
import { GameTurn } from "./turn";

// Interfaces for Game
export interface Game {
  id: string;
  code: string;
  name: string;
  creator: string;
  creationDate: Date;
  state: GameState;
  players: GamePlayer[];
  options: GameOptions;
  map: GameMap;
  currentTurn: GameTurn;
  eventHistory: GameEvent[];
  chat: ChatMessage[];
}

export interface GameOptions {
  turnTime: any;
  maxPlayers: number;
  timePerTurn: number; // in seconds, 0 means no limit
  chatEnabled: boolean;
  specialRules: SpecialRule[];
}
