// src/app/core/models/territory.model.ts

export interface Territory {
  id: string;
  name: string;
  continentId: string;
  continentName: string;
  ownerId?: string;
  ownerName?: string;
  armies: number;
  positionX: number;
  positionY: number;
  neighborIds: string[];
  canBeAttacked: boolean;
  canAttack: boolean;
}

export interface Continent {
  id: string;
  name: string;
  bonusArmies: number;
  territories: Territory[];
  controllerName?: string;
  isControlled: boolean;
  totalCountries: number;
  controlledCountries: number;
}

export interface GameMap {
  territories: Territory[];
  continents: Continent[];
}

export interface Card {
  id: string;
  territoryId: string;
  territoryName: string;
  symbol: 'INFANTRY' | 'CAVALRY' | 'CANNON' | 'WILDCARD';
}

export interface Objective {
  id: string;
  type: 'OCCUPATION' | 'DESTRUCTION' | 'COMMON';
  description: string;
  isAchieved: boolean;
  progressDescription: string;
  targetData?: any;
}

// Clase para manejar territorios
export class TerritoryModel {
  constructor(
    public id: string = '',
    public name: string = '',
    public continentId: string = '',
    public continentName: string = '',
    public armies: number = 0,
    public positionX: number = 0,
    public positionY: number = 0,
    public neighborIds: string[] = [],
    public ownerId?: string,
    public ownerName?: string
  ) {}

  // Verificar si tiene dueño
  isOwned(): boolean {
    return this.ownerId !== undefined && this.ownerId !== null;
  }

  // Verificar si es propiedad de un jugador específico
  isOwnedBy(playerId: string): boolean {
    return this.ownerId === playerId;
  }

  // Verificar si puede atacar
  canAttackWith(armies: number): boolean {
    return this.armies > armies; // Debe quedar al menos 1 ejército
  }

  // Verificar si es vecino de otro territorio
  isNeighborOf(territoryId: string): boolean {
    return this.neighborIds.includes(territoryId);
  }

  // Agregar ejércitos
  addArmies(count: number): void {
    this.armies += count;
  }

  // Remover ejércitos
  removeArmies(count: number): boolean {
    if (this.armies > count) {
      this.armies -= count;
      return true;
    }
    return false;
  }

  // Cambiar dueño
  changeOwner(newOwnerId: string, newOwnerName: string): void {
    this.ownerId = newOwnerId;
    this.ownerName = newOwnerName;
  }

  // Crear desde datos del backend
  static fromApiData(data: any): TerritoryModel {
    return new TerritoryModel(
      data.id,
      data.name,
      data.continentId,
      data.continentName,
      data.armies || 0,
      data.positionX || 0,
      data.positionY || 0,
      data.neighborIds || [],
      data.ownerId,
      data.ownerName
    );
  }
}

// Clase para manejar continentes
export class ContinentModel {
  constructor(
    public id: string = '',
    public name: string = '',
    public bonusArmies: number = 0,
    public territories: TerritoryModel[] = [],
    public controllerName?: string
  ) {}

  // Verificar si está completamente controlado por un jugador
  isControlledBy(playerId: string): boolean {
    return this.territories.length > 0 && 
           this.territories.every(t => t.isOwnedBy(playerId));
  }

  // Obtener número de territorios controlados por un jugador
  getControlledCount(playerId: string): number {
    return this.territories.filter(t => t.isOwnedBy(playerId)).length;
  }

  // Obtener porcentaje de control
  getControlPercentage(playerId: string): number {
    if (this.territories.length === 0) return 0;
    return (this.getControlledCount(playerId) / this.territories.length) * 100;
  }

  // Verificar si un jugador puede recibir bonus
  canPlayerGetBonus(playerId: string): boolean {
    return this.isControlledBy(playerId);
  }

  // Crear desde datos del backend
  static fromApiData(data: any): ContinentModel {
    const territories = (data.territories || []).map((t: any) => TerritoryModel.fromApiData(t));
    
    return new ContinentModel(
      data.id,
      data.name,
      data.bonusArmies || 0,
      territories,
      data.controllerName
    );
  }
}

// Clase para manejar cartas
export class CardModel {
  constructor(
    public id: string = '',
    public territoryId: string = '',
    public territoryName: string = '',
    public symbol: 'INFANTRY' | 'CAVALRY' | 'CANNON' | 'WILDCARD' = 'INFANTRY'
  ) {}

  // Verificar si es wildcard
  isWildcard(): boolean {
    return this.symbol === 'WILDCARD';
  }

  // Obtener símbolo para mostrar
  getSymbolDisplay(): string {
    switch (this.symbol) {
      case 'INFANTRY': return '👨‍💼';
      case 'CAVALRY': return '🐎';
      case 'CANNON': return '🔫';
      case 'WILDCARD': return '⭐';
      default: return '❓';
    }
  }

  // Crear desde datos del backend
  static fromApiData(data: any): CardModel {
    return new CardModel(
      data.id,
      data.territoryId,
      data.territoryName || data.countryName,
      data.symbol || data.type
    );
  }

  // Verificar si tres cartas forman un set válido
  static isValidSet(cards: CardModel[]): boolean {
    if (cards.length !== 3) return false;

    const symbols = cards.map(c => c.symbol);
    const wildcards = symbols.filter(s => s === 'WILDCARD').length;

    // Con wildcards, cualquier combinación es válida
    if (wildcards > 0) return true;

    // Sin wildcards: tres iguales o tres diferentes
    const uniqueSymbols = new Set(symbols);
    return uniqueSymbols.size === 1 || uniqueSymbols.size === 3;
  }
}

// Clase para manejar objetivos
export class ObjectiveModel {
  constructor(
    public id: string = '',
    public type: 'OCCUPATION' | 'DESTRUCTION' | 'COMMON' = 'COMMON',
    public description: string = '',
    public isAchieved: boolean = false,
    public progressDescription: string = '',
    public targetData?: any
  ) {}

  // Verificar si el objetivo está completado
  checkCompletion(gameData: any): boolean {
    // Implementar lógica específica según el tipo de objetivo
    switch (this.type) {
      case 'OCCUPATION':
        return this.checkOccupationObjective(gameData);
      case 'DESTRUCTION':
        return this.checkDestructionObjective(gameData);
      case 'COMMON':
        return this.checkCommonObjective(gameData);
      default:
        return false;
    }
  }

  private checkOccupationObjective(gameData: any): boolean {
    // Lógica para objetivos de ocupación
    return false; // Placeholder
  }

  private checkDestructionObjective(gameData: any): boolean {
    // Lógica para objetivos de destrucción
    return false; // Placeholder
  }

  private checkCommonObjective(gameData: any): boolean {
    // Lógica para objetivo común (30 países)
    return false; // Placeholder
  }

  // Crear desde datos del backend
  static fromApiData(data: any): ObjectiveModel {
    return new ObjectiveModel(
      data.id,
      data.type,
      data.description,
      data.isAchieved || false,
      data.progressDescription || '',
      data.targetData
    );
  }
}