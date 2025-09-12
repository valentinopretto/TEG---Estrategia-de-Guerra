# User Experience (UX) Document
## Project: "TEG"

---

## Main Menu

- **Available Options:**
    - Create Game
    - Join Game
    - Rules
    - About
    - User Settings

- **Possible Actions:**
    - Set up a new game or join an existing one.
    - Access the Rules section for guidance.
    - View project information.
    - Manage user options, such as game history.

---

## Navigation Flow

### 1. Register/Login (Required)

- **Actions:**
    - Fill in the registration/login form.
    - Validate and authenticate credentials.
    - Automatic redirection to the Main Menu.

---

### 2. User Settings

- **Available Actions:**
    - View **Game History** (list of played matches).
    - Change username or avatar.
    - Configure notification preferences.

- **Events:**
    - Immediate update of settings without page reload.

---

### 3. Create Game

- **Actions:**
    - Set:
        - Number of players.
        - Enable chat or silent mode.
        - Enable special rules (optional).
        - Define turn time limit.
    - Confirm to create the game.

- **Events:**
    - Field validation.
    - Automatic game code generation.

---

### 4. Join Game

- **Actions:**
    - Enter game code.
    - Select a game from the available list.

- **Events:**
    - Alerts for incorrect code.
    - Successful entry leads to the Waiting Room.

---

### 5. Waiting Room

- **Actions:**
    - View list of connected players (name and avatar).
    - Wait for the host to start the game.

- **Events:**
    - Vote to suspend the game if delays occur.
    - Animation on game start.

---

### 6. Game Screen

- **Presentation:**
    - Map divided by territories.
    - Sidebar for actions and turn status.
    - Lateral or bottom chat window.
    - Collapsible panel for Objectives (common and secret).
    - **Event History Panel**, visible or collapsible.

- **Available Actions:**
    - **Attack** enemy territories.
    - **Regroup Troops** (strategic movement after attacking).
    - **End Turn** to pass control to the next player.
    - **Pause Button:**
        - **Pause options:** Suspend Game, Abandon Game, Resume Game.

- **Events:**
    - Automatic logging of key actions (e.g., "Player X conquered Territory Y").
    - Brief animations when conquering or losing territories.
    - Notification system for unread chat messages.

---

### 7. In-Game Event History

- **Features:**
    - Each major action (attacks, conquests, movements, quits) is recorded.
    - History can be:
        - Visible in a sidebar panel.
        - Hidden to maximize gameplay space.
    - Option to "Expand" or "Minimize" event history at any time.

---

### 8. Rules

- **Actions:**
    - Read the rulebook divided into sections:
        - Game Setup.
        - Turn Phases.
        - Victory Conditions.
        - Optional Rules.

- **Events:**
    - Quick access from any screen via side menu or button.

---

### 9. About

- **Actions:**
    - View project description, credits, and app version.

---

# Special Interactions

- **Alerts/Modals:**
    - Validation errors.
    - Confirmations for critical actions.
    - Suspension or abandonment confirmations.

- **Notification Indicators:**
    - Discreet dot for new chat messages.
    - Notification of important new events during gameplay.

- **Pause System:**
    - Allows temporary suspension or orderly abandonment of the game.

---

# End of Document

