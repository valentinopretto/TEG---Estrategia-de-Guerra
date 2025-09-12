# Class Diagram - Teg Domain


```plantuml
@startuml
[[!include ./diagrams/class.puml]]
@enduml
```

---
# TEG CLASS DIAGRAM


**Class:** `User`
```java
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private String avatarUrl;

    private List<Player> playerProfiles;
    private List<Invitation> sentInvitations;
    private List<Invitation> receivedInvitations;

    public boolean authenticate(String rawPassword);
    public void updateEmail(String newEmail);
    public void updateAvatar(String url);
}
```


**Class:** `Player`
```java
public class Player {
    private Long id;
    private User user;              // null if bot
    private BotProfile botProfile;  // null if human
    private Objective objective;
    private List<Card> hand;
    private PlayerStatus status;
    private int armiesToPlace;
    private int seatOrder;

    public void placeReinforcements();
    public void performAttack();
    public void performFortify();
    public void tradeCards();

    public boolean isEliminated();
    public boolean hasWon(Game game);
    public boolean isHuman();
    public boolean isBot();
}
```

**Class:** `BotProfile`
```java
public class BotProfile {
    private Long id;
    private BotLevel level;
    private BotStrategy strategy;

    public static BotProfile create(BotLevel level);
    public String getName();
}
```

**Class:** `Game`
```java
public class Game {
    private Long id;
    private List<Player> players;
    private Map<Country, GameTerritory> territories;
    private Deck deck;
    private List<GameEvent> events;
    private GamePhase currentPhase;
    private int currentTurn;
    private CommunicationRules commRules;
    private TurnTimer turnTimer;

    private int maxPlayers;

    public void start();
    public void end();
    public void nextTurn();

    public CombatResult attack(Player attacker, Country from, Country to, List<int> attackerDice, List<int> defenderDice);
    public void reinforce(Player player, Map<Country,Integer> reinforcements);
    public void fortify(Player player, Country from, Country to, int armies);

    public List<Card> drawCards(int count);
    public void tradeCards(Player player, List<Card> cards);

    public boolean isOver();
    public Player getWinner();
    public void saveSnapshot();
    public boolean hasSlot();
    public Player addPlayer(User user);
    public void setOpen(boolean open);
}
```

**Class:** `Country`
```java
public class Country {
    private Long id;
    private String name;
    private Continent continent;
    private Player owner;
    private int armies;
    private Set<Country> neighbors;

    public boolean isNeighbor(Country other);
    public boolean canAttackFrom(Country from);
}
```

**Class:** `Continent`
```java
public class Continent {
    private Long id;
    private String name;
    private int bonusArmies;
    private Set<Country> countries;

    public boolean isControlledBy(Player player);
}
```

**Class:** `Deck`
```java
public class Deck {
    private Deque<Card> drawPile;
    private List<Card> discardPile;

    public Card draw();
    public void discard(List<Card> cards);
    public int remaining();
}
```


**Class:** `Objective`
```java
public class Objective {
    private Long id;
    private ObjectiveType type;  // COMMON, OCCUPATION, DESTRUCTION
    private String description;

    public boolean isAchieved(Game game, Player player);
}
```


**Class:** `GameEvent`
```java
public class GameEvent {
    private Long id;
    private int turnNumber;
    private Player actor;
    private EventType type;
    private String data;  // JSON or serialized structure
    private LocalDateTime timestamp;

    public void apply(GameState state);
}
```


**Class:** `TurnTimer`
```java
public class TurnTimer {
    private Long id;
    private Game game;
    private Player player;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean timedOut;

    public void start();
    public void cancel();
    public boolean hasTimedOut();
}
```


**Class:** `GameSnapshot`
```java
public class GameSnapshot {
    private Long id;
    private Long gameId;
    private int turnNumber;
    private String serializedState;
    private LocalDateTime createdAt;

    public static GameSnapshot createFrom(Game game);
    public void restore(Game game);
}
```


**Class:** `Invitation`
```java
public class Invitation {
    private Long id;
    private Game game;
    private User sender;
    private User recipient;
    private InvitationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime respondedAt;

    public void accept();
    public void decline();
}
```

**Class:** `CommunicationRules`
```java
public class CommunicationRules {
    private boolean chatAllowed;
    private boolean privateAgreementsAllowed;

    public boolean validate(ChatMessage msg);
}
```


**Class:** `CombatResult`
```java
public class CombatResult {
    private Country attackerOrigin;
    private Country defenderTarget;
    private int[] attackerDice;
    private int[] defenderDice;
    private int attackerLosses;
    private int defenderLosses;
    private boolean territoryConquered;
}
```


**Class:** `ChatMessage`
```java
public class ChatMessage {
    private Long id;
    private Player sender;
    private Long gameId;
    private String content;
    private LocalDateTime sentAt;
}
```


**Class:** `RuleViolationReport`
```java
public class RuleViolationReport {
    private Long id;
    private Player reporter;
    private Player reported;
    private String reason;
    private LocalDateTime reportedAt;

    public void vote(Player voter, boolean approve);
}
```


**Class:** `Vote`
```java
public class Vote {
    private RuleViolationReport report;
    private Player voter;
    private boolean approve;
}
```


**Class:** `Card`
```java
public class Card {
    private Long id;
    private Country country;
    private CardType type;  // INFANTRY, CAVALRY, CANNON
}
```
