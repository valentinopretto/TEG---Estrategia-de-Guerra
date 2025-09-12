import { PlayerState } from "../../enums/player-state";
import { TerritoryCard } from "./card";
import { Territory } from "./map";
import { Objective } from "./objective";
import { User } from "./user";
import {BotLevel} from '../../enums/BotLevel';

// Interfaces for Game Player
export interface GamePlayer {
  id: string;  // Cambia de string a number
  username: string | User;
  displayName:string | User;
  status: string;
  color: string;
  isBot: boolean;
  botLevel: BotLevel | null;
  armiesToPlace: number;
  seatOrder: number;
  territoryIds?: number[];
  // Propiedades opcionales que vienen del backend
  objective?: Objective; // O define interfaz Objective
  joinedAt?: Date;
  eliminatedAt?: Date | null;
  hand?: any;
  territoryCount?: number;
  totalArmies?: number | null;
}
