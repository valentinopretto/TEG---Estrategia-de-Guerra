# TEG System Interfaces

```plantuml
@startuml
[[!include ./diagrams/interface.puml]]
@enduml
```

## Introduction

This document describes the interfaces detected in the TEG (Tactical and Strategic War Game) system, the methods they provide, and the abstractions they represent. These interfaces have been identified based on the analysis of the provided UML diagram, user experience document, and database schema.

## System Interfaces

### 1. User Authentication Interface

**Abstraction:** Represents the entry point to the system for users, allowing authentication and basic credential management.

**Methods:**
- `authenticate(rawPassword: String): boolean` - Verifies if the provided password is correct
- `updateEmail(newEmail: String)` - Updates the user's email
- `updateAvatar(url: String)` - Updates the user's profile image
- `registerUser(username: String, password: String, email: String): User` - Registers a new user in the system
- `getAvailableGames(): List<Game>` - Gets the list of available games to join

### 2. Player Management Interface

**Abstraction:** Represents the actions that a player (human or bot) can perform during the game.

**Methods:**
- `placeReinforcements()` - Places reinforcements on controlled territories
- `performAttack()` - Performs an attack against an enemy territory
- `performFortify()` - Performs a fortification movement between owned territories
- `tradeCards()` - Exchanges cards for additional reinforcements
- `isEliminated(): boolean` - Checks if the player has been eliminated
- `hasWon(game: Game): boolean` - Checks if the player has fulfilled their objective and won
- `isHuman(): boolean` - Indicates if the player is human
- `isBot(): boolean` - Indicates if the player is a bot
- `getPlayerHand(): List<Card>` - Gets the cards in the player's hand

### 3. Bot Strategy Interface

**Abstraction:** Defines the strategic behavior of bots according to their difficulty level.

**Methods:**
- `reinforce(): void` - Implements the reinforcement strategy
- `attack(): CombatResult` - Implements the attack strategy
- `fortify(): void` - Implements the fortification strategy
- `getBotDifficulty(): int` - Gets the bot's difficulty level

### 4. Game Control Interface

**Abstraction:** Represents the main operations to manage the game flow.

**Methods:**
- `start()` - Starts the game
- `end()` - Ends the game
- `nextTurn()` - Advances to the next turn
- `attack(attacker, from, to, dA, dD): CombatResult` - Executes an attack
- `reinforce(player, placements)` - Executes the reinforcement phase
- `fortify(player, from, to, armies)` - Executes the fortification phase
- `drawCards(count: int): List<Card>` - Draws cards from the deck
- `tradeCards(player, cards: List<Card>)` - Processes the exchange of cards
- `isOver(): boolean` - Checks if the game has ended
- `getWinner(): Player` - Gets the winning player
- `saveSnapshot()` - Saves a snapshot of the current game state
- `hasSlot(): boolean` - Checks if there is space for more players
- `isOpen(): boolean` - Checks if the game is open to new players
- `addPlayer(user: User): Player` - Adds a player to the game
- `setOpen(open: boolean)` - Sets whether the game is open or not
- `getCurrentPhase(): Phase` - Gets the current game phase
- `getCurrentRound(): int` - Gets the current game round

### 5. Territory Management Interface

**Abstraction:** Represents operations related to game territories.

**Methods:**
- `isNeighbor(other: Country): boolean` - Checks if a territory is a neighbor of another
- `canAttackFrom(from: Country): boolean` - Checks if an attack can be launched from a specific territory
- `getCountryById(id: int): Country` - Gets a country by its ID
- `getCountriesByPlayer(player: Player): List<Country>` - Gets all countries owned by a player
- `getCountriesByContinent(continent: Continent): List<Country>` - Gets all countries in a continent
- `updateCountryOwnership(country: Country, player: Player)` - Updates the ownership of a country
- `updateCountryArmies(country: Country, armies: int)` - Updates the number of armies in a country

### 6. Continent Management Interface

**Abstraction:** Represents operations related to continents in the game.

**Methods:**
- `isControlledBy(player: Player): boolean` - Checks if a continent is controlled by a player
- `getContinentById(id: int): Continent` - Gets a continent by its ID
- `getAllContinents(): List<Continent>` - Gets all continents in the game
- `getContinentBonus(continent: Continent): int` - Gets the bonus armies for controlling a continent

### 7. Deck Management Interface

**Abstraction:** Represents operations related to the card deck.

**Methods:**
- `draw(): Card` - Draws a card from the deck
- `discard(cards: List<Card>)` - Discards a list of cards
- `remaining(): int` - Returns the number of cards remaining in the deck
- `getCardById(id: int): Card` - Gets a card by its ID
- `getCardsByPlayer(player: Player): List<Card>` - Gets all cards held by a player
- `isCardUsed(card: Card): boolean` - Checks if a card has been used

### 8. Objectives Interface

**Abstraction:** Represents the verification of objectives to determine victory.

**Methods:**
- `isAchieved(game: Game, player: Player): boolean` - Checks if an objective has been fulfilled
- `getObjectiveById(id: int): Objective` - Gets an objective by its ID
- `getObjectivesByType(type: TypeObjective): List<Objective>` - Gets objectives by type
- `getObjectiveDescription(objective: Objective): String` - Gets the description of an objective

### 9. Game Events Interface

**Abstraction:** Represents events that occur during the game and their application.

**Methods:**
- `apply(state: GameState)` - Applies the event to the game state
- `getEventById(id: int): GameEvent` - Gets a game event by its ID
- `getEventsByGame(game: Game): List<GameEvent>` - Gets all events in a game
- `getEventsByPlayer(player: Player): List<GameEvent>` - Gets all events by a specific player
- `getEventsByAction(action: Action): List<GameEvent>` - Gets all events of a specific action type
- `createEvent(game: Game, player: Player, action: Action, info: String): GameEvent` - Creates a new game event

### 10. Turn Timer Interface

**Abstraction:** Manages the time limit for player turns.

**Methods:**
- `start()` - Starts the timer
- `cancel()` - Cancels the timer
- `hasTimedOut(): boolean` - Checks if time has run out
- `getActiveTimerByGame(game: Game): TurnTimer` - Gets the active timer for a game
- `getRemainingTime(): int` - Gets the remaining time in seconds

### 11. Game Snapshot Interface

**Abstraction:** Manages saving and restoring game states.

**Methods:**
- `createFrom(game: Game): GameSnapshot` - Creates a snapshot from the current state
- `restore(game: Game)` - Restores the game from a snapshot
- `getSnapshotsByGame(game: Game): List<GameSnapshot>` - Gets all snapshots for a game
- `getLatestSnapshot(game: Game): GameSnapshot` - Gets the latest snapshot for a game

### 12. Invitation Management Interface

**Abstraction:** Handles invitations between users to participate in games.

**Methods:**
- `accept()` - Accepts an invitation
- `decline()` - Declines an invitation
- `sendInvitation(sender: User, recipient: User, game: Game): Invitation` - Sends a new invitation
- `getInvitationsByUser(user: User): List<Invitation>` - Gets all invitations for a user
- `getPendingInvitations(user: User): List<Invitation>` - Gets pending invitations for a user

### 13. Communication Rules Interface

**Abstraction:** Verifies that chat messages comply with established rules.

**Methods:**
- `validate(msg: ChatMessage): boolean` - Validates if a message complies with communication rules
- `isChatEnabled(game: Game): boolean` - Checks if chat is enabled for a game

### 14. Combat Interface

**Abstraction:** Represents the result of combat between territories.

**Methods:**
- `calculateCombatResult(attacker: Country, defender: Country, attackerDice: int, defenderDice: int): CombatResult` - Calculates the result of a combat
- `resolveAttack(combatResult: CombatResult): void` - Resolves the outcome of an attack
- `getAttackerLosses(): int` - Gets the number of armies lost by the attacker
- `getDefenderLosses(): int` - Gets the number of armies lost by the defender
- `isTerritoryConquered(): boolean` - Checks if the territory was conquered

### 15. Main Menu Interface

**Abstraction:** Represents the entry point to the different game functionalities.

**Methods:**
- `createGame()` - Initiates the process of creating a new game
- `joinGame()` - Allows joining an existing game
- `showRules()` - Shows the game rules

### 16. Game Configuration Interface

**Abstraction:** Allows configuring parameters for a new game.

**Methods:**
- `setPlayerCount(count: int)` - Sets the number of players
- `enableChat(enabled: boolean)` - Enables or disables chat
- `enableSpecialRules(enabled: boolean)` - Enables or disables special rules
- `setTurnTimeLimit(minutes: int)` - Sets the time limit per turn
- `createGame(): Game` - Creates the game with the established configuration
- `saveGameRules(rules: String)` - Saves the game rules in text format

### 17. Waiting Room Interface

**Abstraction:** Manages the state prior to starting the game.

**Methods:**
- `getConnectedPlayers(): List<Player>` - Gets the list of connected players
- `startGame()` - Starts the game (host only)
- `changeHost(player: Player)` - Changes the host of the game
- `isGameReady(): boolean` - Checks if all conditions are met to start the game

### 18. Game Screen Interface

**Abstraction:** Allows interaction with the current game state.

**Methods:**
- `attack(from: Country, to: Country, armies: int)` - Performs an attack
- `regroupTroops(from: Country, to: Country, armies: int)` - Regroups troops
- `endTurn()` - Ends the current turn
- `pauseGame()` - Pauses the game
- `resumeGame()` - Resumes the paused game
- `abandonGame()` - Abandons the game
- `getCurrentGameState(): GameState` - Gets the current game state

### 19. Event History Interface

**Abstraction:** Allows viewing and managing the game event log.

**Methods:**
- `getEvents(): List<GameEvent>` - Gets the list of events

### 20. Chat Interface

**Abstraction:** Allows communication between players during the game.

**Methods:**
- `sendMessage(content: String)` - Sends a message
- `getMessages(): List<ChatMessage>` - Gets the list of messages
- `hasUnreadMessages(): boolean` - Checks if there are unread messages
- `getMessageById(id: int): ChatMessage` - Gets a message by its ID
- `getMessagesByGame(game: Game): List<ChatMessage>` - Gets all messages in a game
- `getMessagesByPlayer(player: Player): List<ChatMessage>` - Gets all messages from a specific player

### 21. Game State Interface

**Abstraction:** Manages the different states a game can be in.

**Methods:**
- `getStateById(id: int): State` - Gets a state by ID
- `getAllStates(): List<State>` - Gets all possible game states
- `changeGameState(game: Game, state: State)` - Changes the state of a game
- `isGameActive(game: Game): boolean` - Checks if a game is in an active state

### 22. Border Management Interface

**Abstraction:** Manages the borders between countries.

**Methods:**
- `getBorderById(id: int): Border` - Gets a border by ID
- `getBordersBetweenCountries(country1: Country, country2: Country): List<Border>` - Gets borders between two countries
- `getAllBorders(): List<Border>` - Gets all borders in the game map
- `getNeighborCountries(country: Country): List<Country>` - Gets all neighbor countries of a given country

## Conclusion

The identified interfaces provide a clear structure for the various functionalities of the TEG system, taking into account the database schema provided. Each interface represents a specific abstraction, and its associated methods allow for implementing the necessary logic for the game's operation, meeting the requirements established in the user experience document and following the structure established in the UML diagram and database schema.

These interfaces cover all aspects of the system, from user authentication and game management to territory control and game events, ensuring proper implementation of the game mechanics and user interaction.
