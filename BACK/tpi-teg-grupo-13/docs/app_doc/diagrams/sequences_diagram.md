# Sequences Diagrams


```plantuml
@startuml
[[!include ./diagrams/sequences.puml]]
@enduml
```
# Sequence Diagram – Player Attack Flow

This diagram illustrates the flow when a player performs an attack action during their turn.

## Steps

1. **User Action**: The player selects a source and target country and clicks "Attack".
2. **Frontend**: Sends a REST POST request with attack details to the backend.
3. **Backend**:
   - Validates that the attack is allowed (based on current game phase, adjacency, army count).
   - Performs the dice roll simulation.
   - Updates the game state (territory ownership, troop counts).
4. **Database**: Updated state is saved to SQL Server.
5. **Real-Time Feedback**:
   - The attack result is published to Redis.
   - Redis notifies all connected clients via WebSocket.
6. **Frontend**: Receives the update and renders the result visually to the players.

