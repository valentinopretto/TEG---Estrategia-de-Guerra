export interface User {
  id: string;
  username: string;
  email: string;
  avatarUrl?: string;
  gamesPlayed: number;
  gamesWon: number;
  createdAt: Date;
  lastLogin?: Date;
  isActive: boolean;
}

export interface UserStats {
  totalGames: number;
  wins: number;
  losses: number;
  winRate: number;
  currentStreak: number;
  bestStreak: number;
  averageGameDuration: number; // en minutos
  favoriteStrategy?: string;
  totalPlayTime: number; // en minutos
}

export interface UserProfile {
  user: User;
  stats: UserStats;
}

export class UserModel {
  constructor(
    public id: string = '',
    public username: string = '',
    public email: string = '',
    public avatarUrl: string = '',
    public gamesPlayed: number = 0,
    public gamesWon: number = 0,
    public createdAt: Date = new Date(),
    public lastLogin?: Date,
    public isActive: boolean = true
  ) {}

  getWinRate(): number {
    if (this.gamesPlayed === 0) return 0;
    return Math.round((this.gamesWon / this.gamesPlayed) * 100);
  }

  getDisplayInfo(): string {
    return `${this.username} (${this.getWinRate()}% victorias)`;
  }

  isActivePlayer(): boolean {
    return this.isActive && this.lastLogin !== undefined;
  }

  static fromApiData(data: any): UserModel {
    return new UserModel(
      data.id,
      data.username,
      data.email,
      data.avatarUrl || '',
      data.gamesPlayed || 0,
      data.gamesWon || 0,
      new Date(data.createdAt),
      data.lastLogin ? new Date(data.lastLogin) : undefined,
      data.isActive ?? true
    );
  }

  toApiData(): Partial<User> {
    return {
      id: this.id,
      username: this.username,
      email: this.email,
      avatarUrl: this.avatarUrl,
      gamesPlayed: this.gamesPlayed,
      gamesWon: this.gamesWon,
      isActive: this.isActive
    };
  }
}