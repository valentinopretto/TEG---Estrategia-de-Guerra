import { TerritoryState } from "../../enums/territory-state";

// Interfaces for Game Map
export interface GameMap {
  continents: Continent[];
  territories: Territory[];
  connections: Connection[];
}

export interface Continent {
  id: string;
  name: string;
  color: string;
  bonus: number; // Additional troops for controlling the entire continent
  territories: string[]; // IDs of territories
}

export interface Territory {
  id: string;
  name: string;
  continentId: string;
  ownerId: string | null;
  troops: number;
  position: Coordinates;
  connections: string[]; // IDs of connected territories
  state: TerritoryState;
}

export interface Connection {
  from: string; // Origin territory ID
  to: string;   // Destination territory ID
}

export interface Coordinates {
  x: number;
  y: number;
}
