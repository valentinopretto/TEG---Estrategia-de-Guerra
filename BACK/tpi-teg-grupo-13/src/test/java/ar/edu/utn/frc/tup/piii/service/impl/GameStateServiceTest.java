package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameStateServiceTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameStateServiceImpl gameStateService;

    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        // Setup players
        player1 = Player.builder()
                .id(1L)
                .seatOrder(0)
                .status(PlayerStatus.ACTIVE)
                .build();

        player2 = Player.builder()
                .id(2L)
                .seatOrder(1)
                .status(PlayerStatus.ACTIVE)
                .build();

        // Setup game
        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(TurnPhase.REINFORCEMENT)
                .currentTurn(1)
                .currentPlayerIndex(0)
                .maxPlayers(6)
                .players(Arrays.asList(player1, player2))
                .build();
    }

    @Test
    void changeGameState_FromWaitingToReinforcement5_ShouldSucceed() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.REINFORCEMENT_5);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.REINFORCEMENT_5);
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.REINFORCEMENT);
    }

    @Test
    void changeGameState_FromReinforcement5ToReinforcement3_ShouldSucceed() {
        // Given
        game.setState(GameState.REINFORCEMENT_5);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.REINFORCEMENT_3);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.REINFORCEMENT_3);
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.REINFORCEMENT);
    }

    @Test
    void changeGameState_FromReinforcement3ToHostilityOnly_ShouldSucceed() {
        // Given
        game.setState(GameState.REINFORCEMENT_3);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.HOSTILITY_ONLY);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.HOSTILITY_ONLY);
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.ATTACK);
    }

    @Test
    void changeGameState_FromHostilityOnlyToNormalPlay_ShouldSucceed() {
        // Given
        game.setState(GameState.HOSTILITY_ONLY);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.NORMAL_PLAY);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.NORMAL_PLAY);
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.REINFORCEMENT);
    }

    @Test
    void changeGameState_FromNormalPlayToPaused_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.PAUSED);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.PAUSED);
    }

    @Test
    void changeGameState_FromNormalPlayToFinished_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.FINISHED);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.FINISHED);
    }

    @Test
    void changeGameState_FromPausedToNormalPlay_ShouldSucceed() {
        // Given
        game.setState(GameState.PAUSED);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.NORMAL_PLAY);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.NORMAL_PLAY);
    }

    @Test
    void changeGameState_FromFinished_ShouldFail() {
        // Given
        game.setState(GameState.FINISHED);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.NORMAL_PLAY);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void changeGameState_InvalidTransition_ShouldFail() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);

        // When
        boolean result = gameStateService.changeGameState(game, GameState.NORMAL_PLAY);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void changeTurnPhase_FromReinforcementToAttackInNormalPlay_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.ATTACK);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.ATTACK);
    }

    @Test
    void changeTurnPhase_FromAttackToFortify_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.FORTIFY);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.FORTIFY);
    }

    @Test
    void changeTurnPhase_FromAttackToEndTurn_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.END_TURN);
    }

    @Test
    void changeTurnPhase_FromFortifyToClaimCard_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.CLAIM_CARD);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.CLAIM_CARD);
    }

    @Test
    void changeTurnPhase_FromFortifyToEndTurn_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.END_TURN);
    }

    @Test
    void changeTurnPhase_FromClaimCardToEndTurn_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.CLAIM_CARD);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.END_TURN);
    }

    @Test
    void changeTurnPhase_FromEndTurnToReinforcement_ShouldSucceed() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.END_TURN);
        game.setCurrentPlayerIndex(0);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.REINFORCEMENT);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.REINFORCEMENT);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(1); // Next player
    }

    @Test
    void changeTurnPhase_InWaitingState_ShouldFail() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.ATTACK);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void changeTurnPhase_ReinforcementToEndTurnInInitialPhases_ShouldSucceed() {
        // Given
        game.setState(GameState.REINFORCEMENT_5);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.END_TURN);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.END_TURN);
    }

    @Test
    void changeTurnPhase_ReinforcementToAttackInHostilityOnly_ShouldSucceed() {
        // Given
        game.setState(GameState.HOSTILITY_ONLY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        boolean result = gameStateService.changeTurnPhase(game, TurnPhase.ATTACK);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.ATTACK);
    }

    @Test
    void startGame_WhenCanStart_ShouldReturnTrue() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);

        // When
        boolean result = gameStateService.startGame(game);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.REINFORCEMENT_5);
    }

    @Test
    void pauseGame_WhenInProgress_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);

        // When
        boolean result = gameStateService.pauseGame(game);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.PAUSED);
    }

    @Test
    void resumeGame_WhenPaused_ShouldReturnTrue() {
        // Given
        game.setState(GameState.PAUSED);

        // When
        boolean result = gameStateService.resumeGame(game);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.NORMAL_PLAY);
    }

    @Test
    void finishGame_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);

        // When
        boolean result = gameStateService.finishGame(game);

        // Then
        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.FINISHED);
    }

    @Test
    void nextTurn_ShouldAdvanceToNextPlayer() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPlayerIndex(0);
        game.setCurrentTurn(1);

        // When
        gameStateService.nextTurn(game);

        // Then
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(1);
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.REINFORCEMENT);
    }

    @Test
    void nextTurn_WhenLastPlayer_ShouldWrapToFirstAndIncrementTurn() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPlayerIndex(1); // Last player
        game.setCurrentTurn(1);

        // When
        gameStateService.nextTurn(game);

        // Then
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(game.getCurrentTurn()).isEqualTo(2);
    }

    @Test
    void nextTurn_InReinforcement5_ShouldTransitionToReinforcement3() {
        // Given
        game.setState(GameState.REINFORCEMENT_5);
        game.setCurrentPlayerIndex(1); // Complete round

        // When
        gameStateService.nextTurn(game);

        // Then
        assertThat(game.getState()).isEqualTo(GameState.REINFORCEMENT_3);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
    }

    @Test
    void nextTurn_InReinforcement3_ShouldTransitionToHostilityOnly() {
        // Given
        game.setState(GameState.REINFORCEMENT_3);
        game.setCurrentPlayerIndex(1); // Complete round

        // When
        gameStateService.nextTurn(game);

        // Then
        assertThat(game.getState()).isEqualTo(GameState.HOSTILITY_ONLY);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
    }

    @Test
    void nextTurn_InHostilityOnly_ShouldTransitionToNormalPlay() {
        // Given
        game.setState(GameState.HOSTILITY_ONLY);
        game.setCurrentPlayerIndex(1); // Complete round

        // When
        gameStateService.nextTurn(game);

        // Then
        assertThat(game.getState()).isEqualTo(GameState.NORMAL_PLAY);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
    }

    @Test
    void canPerformAction_ReinforceInReinforcementPhase_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        boolean result = gameStateService.canPerformAction(game, "reinforce");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_AttackInAttackPhase_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        boolean result = gameStateService.canPerformAction(game, "attack");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_FortifyInFortifyPhase_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        // When
        boolean result = gameStateService.canPerformAction(game, "fortify");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_EndTurnInEndTurnPhase_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.END_TURN);

        // When
        boolean result = gameStateService.canPerformAction(game, "end_turn");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_SkipAttackInAttackPhase_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        boolean result = gameStateService.canPerformAction(game, "skip_attack");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_SkipFortifyInFortifyPhase_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        // When
        boolean result = gameStateService.canPerformAction(game, "skip_fortify");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_ClaimCardInClaimCardPhase_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.CLAIM_CARD);

        // When
        boolean result = gameStateService.canPerformAction(game, "claim_card");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_InvalidAction_ShouldReturnFalse() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        boolean result = gameStateService.canPerformAction(game, "invalid_action");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canPerformAction_WhenGameNotActive_ShouldReturnFalse() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        boolean result = gameStateService.canPerformAction(game, "reinforce");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canPerformAction_AttackInHostilityOnly_ShouldReturnTrue() {
        // Given
        game.setState(GameState.HOSTILITY_ONLY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        boolean result = gameStateService.canPerformAction(game, "attack");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformAction_AttackInReinforcement5_ShouldReturnFalse() {
        // Given
        game.setState(GameState.REINFORCEMENT_5);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        boolean result = gameStateService.canPerformAction(game, "attack");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getAvailableActions_InReinforcementPhaseNormalPlay_ShouldReturnCorrectActions() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        String[] actions = gameStateService.getAvailableActions(game);

        // Then
        assertThat(actions).contains("reinforce", "proceed_to_attack");
    }

    @Test
    void getAvailableActions_InAttackPhase_ShouldReturnCorrectActions() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        String[] actions = gameStateService.getAvailableActions(game);

        // Then
        assertThat(actions).contains("attack", "skip_attack", "proceed_to_fortify");
    }

    @Test
    void getAvailableActions_InFortifyPhase_ShouldReturnCorrectActions() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        // When
        String[] actions = gameStateService.getAvailableActions(game);

        // Then
        assertThat(actions).contains("fortify", "skip_fortify", "finish_fortify");
    }

    @Test
    void getAvailableActions_InClaimCardPhase_ShouldReturnCorrectActions() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.CLAIM_CARD);

        // When
        String[] actions = gameStateService.getAvailableActions(game);

        // Then
        assertThat(actions).contains("claim_card", "skip_claim_card");
    }

    @Test
    void getAvailableActions_InEndTurnPhase_ShouldReturnCorrectActions() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.END_TURN);

        // When
        String[] actions = gameStateService.getAvailableActions(game);

        // Then
        assertThat(actions).contains("end_turn");
    }

    @Test
    void getAvailableActions_InInitialReinforcementPhase_ShouldReturnCorrectActions() {
        // Given
        game.setState(GameState.REINFORCEMENT_5);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        String[] actions = gameStateService.getAvailableActions(game);

        // Then
        assertThat(actions).contains("reinforce", "end_turn");
    }

    @Test
    void getAvailableActions_WhenNotActive_ShouldReturnEmptyArray() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);

        // When
        String[] actions = gameStateService.getAvailableActions(game);

        // Then
        assertThat(actions).isEmpty();
    }

    @Test
    void isGameActive_WhenNormalPlay_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);

        // When
        boolean result = gameStateService.isGameActive(game);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isGameActive_WhenWaitingForPlayers_ShouldReturnFalse() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);

        // When
        boolean result = gameStateService.isGameActive(game);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isGameActive_WhenPaused_ShouldReturnFalse() {
        // Given
        game.setState(GameState.PAUSED);

        // When
        boolean result = gameStateService.isGameActive(game);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isGameActive_WhenFinished_ShouldReturnFalse() {
        // Given
        game.setState(GameState.FINISHED);

        // When
        boolean result = gameStateService.isGameActive(game);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPlayerTurn_WhenIsPlayersTurn_ShouldReturnTrue() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPlayerIndex(0);

        // When
        boolean result = gameStateService.isPlayerTurn(game, 1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isPlayerTurn_WhenNotPlayersTurn_ShouldReturnFalse() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPlayerIndex(0);

        // When
        boolean result = gameStateService.isPlayerTurn(game, 2L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPlayerTurn_WhenGameNotActive_ShouldReturnFalse() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);
        game.setCurrentPlayerIndex(0);

        // When
        boolean result = gameStateService.isPlayerTurn(game, 1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getCurrentPhaseDescription_InReinforcementPhaseNormalPlay_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Place your reinforcement armies on your territories");
    }

    @Test
    void getCurrentPhaseDescription_InReinforcement5_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.REINFORCEMENT_5);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Place your 5 initial armies on your territories");
    }

    @Test
    void getCurrentPhaseDescription_InReinforcement3_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.REINFORCEMENT_3);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Place your 3 initial armies on your territories");
    }

    @Test
    void getCurrentPhaseDescription_InAttackPhaseNormalPlay_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Attack enemy territories or proceed to fortification");
    }

    @Test
    void getCurrentPhaseDescription_InAttackPhaseHostilityOnly_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.HOSTILITY_ONLY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Attack enemy territories (hostility phase)");
    }

    @Test
    void getCurrentPhaseDescription_InFortifyPhaseNormalPlay_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Move armies between your connected territories or end turn");
    }

    @Test
    void getCurrentPhaseDescription_InFortifyPhaseHostilityOnly_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.HOSTILITY_ONLY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Move armies between your adjacent territories (hostility phase)");
    }

    @Test
    void getCurrentPhaseDescription_InClaimCardPhase_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.CLAIM_CARD);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Claim a country card if you conquered at least one territory");
    }

    @Test
    void getCurrentPhaseDescription_InEndTurnPhase_ShouldReturnCorrectDescription() {
        // Given
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.END_TURN);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Confirm to end your turn");
    }

    @Test
    void getCurrentPhaseDescription_WhenGameNotActive_ShouldReturnCorrectMessage() {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);

        // When
        String description = gameStateService.getCurrentPhaseDescription(game);

        // Then
        assertThat(description).isEqualTo("Game is not active");
    }

    @Test
    void isValidPhaseTransition_FromReinforcementToAttack_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidPhaseTransition(TurnPhase.REINFORCEMENT, TurnPhase.ATTACK);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidPhaseTransition_FromReinforcementToEndTurn_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidPhaseTransition(TurnPhase.REINFORCEMENT, TurnPhase.END_TURN);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidPhaseTransition_FromAttackToFortify_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidPhaseTransition(TurnPhase.ATTACK, TurnPhase.FORTIFY);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidPhaseTransition_FromFortifyToClaimCard_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidPhaseTransition(TurnPhase.FORTIFY, TurnPhase.CLAIM_CARD);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidPhaseTransition_FromClaimCardToEndTurn_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidPhaseTransition(TurnPhase.CLAIM_CARD, TurnPhase.END_TURN);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidPhaseTransition_FromEndTurnToReinforcement_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidPhaseTransition(TurnPhase.END_TURN, TurnPhase.REINFORCEMENT);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidPhaseTransition_InvalidTransition_ShouldReturnFalse() {
        // When
        boolean result = gameStateService.isValidPhaseTransition(TurnPhase.REINFORCEMENT, TurnPhase.CLAIM_CARD);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValidStateTransition_FromWaitingToReinforcement5_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidStateTransition(GameState.WAITING_FOR_PLAYERS, GameState.REINFORCEMENT_5);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidStateTransition_FromReinforcement5ToReinforcement3_ShouldReturnTrue() {
        // When
        boolean result = gameStateService.isValidStateTransition(GameState.REINFORCEMENT_5, GameState.REINFORCEMENT_3);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidStateTransition_FromFinished_ShouldReturnFalse() {
        // When
        boolean result = gameStateService.isValidStateTransition(GameState.FINISHED, GameState.NORMAL_PLAY);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void executeGameAction_SkipAttack_ShouldAdvanceToFortify() {
        // Given
        Long gameId = 1L;
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.ATTACK);

        when(gameService.findById(gameId)).thenReturn(game);
        when(gameService.save(game)).thenReturn(game);

        // When
        String result = gameStateService.executeGameAction(gameId, "skip_attack");

        // Then
        assertThat(result).isEqualTo("Action performed: skip_attack");
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.FORTIFY);
        verify(gameService).save(game);
    }

    @Test
    void executeGameAction_SkipFortify_ShouldAdvanceToEndTurn() {
        // Given
        Long gameId = 1L;
        game.setState(GameState.NORMAL_PLAY);
        game.setCurrentPhase(TurnPhase.FORTIFY);

        when(gameService.findById(gameId)).thenReturn(game);
        when(gameService.save(game)).thenReturn(game);

        // When
        String result = gameStateService.executeGameAction(gameId, "skip_fortify");

        // Then
        assertThat(result).isEqualTo("Action performed: skip_fortify");
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.END_TURN);
        verify(gameService).save(game);
    }

    @Test
    void executeGameAction_UnknownAction_ShouldThrowException() {
        // Given
        Long gameId = 1L;
        when(gameService.findById(gameId)).thenReturn(game);

        // When & Then
        assertThatThrownBy(() -> gameStateService.executeGameAction(gameId, "unknown_action"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown action: unknown_action");
    }

    @Test
    void executeGameAction_InvalidAction_ShouldThrowException() {
        // Given
        Long gameId = 1L;
        game.setState(GameState.WAITING_FOR_PLAYERS);
        when(gameService.findById(gameId)).thenReturn(game);

        // When & Then
        assertThatThrownBy(() -> gameStateService.executeGameAction(gameId, "next_phase"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Action 'next_phase' not allowed in current phase");
    }
}