export interface AttackAction {
  attackerCountryId: string;
  defenderCountryId: string;
  attackingArmies: number
}

export interface AttackResult {
  attackerCountryId: string;
  attackerCountryName: string;
  defenderCountryId: string;
  defenderCountryName: string;
  attackerPlayerName: string;
  defenderPlayerName: string;
  attackerDice: number[];
  defenderDice: number[];
  attackerLosses: number;
  defenderLosses: number;
  territoryConquered: boolean;
  attackerRemainingArmies: number;
  defenderRemainingArmies: number;
}

export interface ReinforcementAction {
  reinforcements: { [countryId: string]: number };
  totalArmies: number;
}

export interface FortifyAction {
  fromCountryId: string;
  toCountryId: string;
  armies: number;
}

export interface CardTradeAction {
  cardIds: string[];
}

export interface GameEvent {
  id: string;
  gameId: string;
  playerId: string;
  playerName: string;
  type: 'ATTACK' | 'CONQUEST' | 'REINFORCEMENT' | 'FORTIFY' | 'TRADE' | 'TURN_START' | 'TURN_END' | 'ELIMINATION' | 'VICTORY';
  description: string;
  timestamp: Date;
  data?: any;
}

// Clases para manejar acciones del juego
export class GameActionModel {
  
  // Crear acción de ataque
  static createAttack(
    attackerCountryId: string,
    defenderCountryId: string,
    attackingArmies: number,
  ): AttackAction {
    return {
      attackerCountryId,
      defenderCountryId,
      attackingArmies,
    };
  }

  // Crear acción de refuerzo
  static createReinforcement(reinforcements: { [countryId: string]: number }): ReinforcementAction {
    const totalArmies = Object.values(reinforcements).reduce((sum, armies) => sum + armies, 0);
    return {
      reinforcements,
      totalArmies
    };
  }

  // Crear acción de reagrupamiento
  static createFortify(fromCountryId: string, toCountryId: string, armies: number): FortifyAction {
    return {
      fromCountryId,
      toCountryId,
      armies
    };
  }

  // Crear acción de intercambio de cartas
  static createCardTrade(cardIds: string[]): CardTradeAction {
    return {
      cardIds
    };
  }
}

export class GameEventModel {
  constructor(
    public id: string = '',
    public gameId: string = '',
    public playerId: string = '',
    public playerName: string = '',
    public type: string = '',
    public description: string = '',
    public timestamp: Date = new Date(),
    public data?: any
  ) {}

  // Obtener tiempo transcurrido
  getTimeAgo(): string {
    const now = new Date();
    const diffMs = now.getTime() - this.timestamp.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'ahora';
    if (diffMins < 60) return `hace ${diffMins}m`;
    if (diffMins < 1440) return `hace ${Math.floor(diffMins / 60)}h`;
    return `hace ${Math.floor(diffMins / 1440)}d`;
  }

  // Crear desde datos del backend
  static fromApiData(data: any): GameEventModel {
    return new GameEventModel(
      data.id,
      data.gameId,
      data.playerId || data.actorId,
      data.playerName || data.actorName,
      data.type,
      data.description || data.message,
      new Date(data.timestamp),
      data.data
    );
  }

  // Crear eventos específicos
  static createAttackEvent(
    gameId: string,
    attackerName: string,
    defenderName: string,
    fromCountry: string,
    toCountry: string,
    conquered: boolean
  ): GameEventModel {
    const description = conquered 
      ? `${attackerName} conquistó ${toCountry} desde ${fromCountry}`
      : `${attackerName} atacó ${toCountry} desde ${fromCountry}`;
    
    return new GameEventModel(
      Date.now().toString(),
      gameId,
      '',
      attackerName,
      'ATTACK',
      description,
      new Date(),
      { fromCountry, toCountry, conquered, defenderName }
    );
  }

  static createReinforcementEvent(
    gameId: string,
    playerName: string,
    totalArmies: number
  ): GameEventModel {
    return new GameEventModel(
      Date.now().toString(),
      gameId,
      '',
      playerName,
      'REINFORCEMENT',
      `${playerName} colocó ${totalArmies} ejércitos`,
      new Date(),
      { totalArmies }
    );
  }

  static createEliminationEvent(
    gameId: string,
    eliminatedPlayerName: string,
    eliminatorName: string
  ): GameEventModel {
    return new GameEventModel(
      Date.now().toString(),
      gameId,
      '',
      eliminatorName,
      'ELIMINATION',
      `${eliminatedPlayerName} ha sido eliminado por ${eliminatorName}`,
      new Date(),
      { eliminatedPlayer: eliminatedPlayerName }
    );
  }

  static createVictoryEvent(
    gameId: string,
    winnerName: string
  ): GameEventModel {
    return new GameEventModel(
      Date.now().toString(),
      gameId,
      '',
      winnerName,
      'VICTORY',
      `¡${winnerName} ha ganado la partida!`,
      new Date(),
      { winner: winnerName }
    );
  }
}