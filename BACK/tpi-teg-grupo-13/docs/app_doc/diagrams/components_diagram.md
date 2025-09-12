# Components Diagrams

```plantuml
@startuml
[[!include ./diagrams/components.puml]]
@enduml
```


This component diagram represents the main technical architecture of the system.

## Components

- **Frontend (Game UI)**: Built in Angular or plain JavaScript. Handles user interaction, game visualization, and communicates with the backend via REST and WebSocket.
- **Backend (Spring Boot)**:
  - `GameController`: Handles REST requests related to the game.
  - `GameService`: Contains the core business logic of the game.
  - `GameStateManager`: Manages in-memory state and snapshots.
  - `ChatController`: Handles WebSocket chat and game events.
  - `BotEngine`: Executes Bot moves for bots during their turn.
- **Data Storage**:
  - `SQL Server`: Stores games, players, countries, cards, events, etc.
  - `Redis`: Used for real-time communication (chat and game events).

## Interaction Overview

1. Frontend sends actions to the backend via REST.
2. Backend processes actions and updates game state.
3. Updates and chat messages are pushed to all players using WebSocket + Redis.
