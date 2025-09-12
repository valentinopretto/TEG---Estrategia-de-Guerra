// Interfaces for User
export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
  gamesPlayed: number;
  gamesWon: number;
  notificationSettings: NotificationSettings;
}

export interface NotificationSettings {
  turn: boolean;
  attack: boolean;
  chatMessage: boolean;
  gameEnd: boolean;
  soundEnabled: boolean;
}
