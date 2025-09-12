export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api', // URL de tu backend Spring Boot
  version: '1.0.0',
  debugMode: true,
  development: {
    skipAuth: false,
    showDebugInfo: true,
    enableMockData: false
  },
  features: {
    chat: true,
    bots: true
  },
  game: {
    maxPlayers: 6,
    minPlayers: 2,
    defaultTurnTime: 60, 
    maxTurnTime: 300, 
    autoSaveInterval: 30000, 
  },
  ui: {
    notifications: {
      duration: 5000,
      maxNotifications: 50
    },
    polling: {
      gameState: 1000, 
      chat: 1000, 
      events: 1000 
    }
  }
};

