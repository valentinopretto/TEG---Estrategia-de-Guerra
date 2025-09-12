package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyExecutor;
import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyFactory;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.repository.BotProfileRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.BotService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio enfocado exclusivamente en la ejecución de lógica de bots.
 * No maneja CRUD de players (eso lo hace GameService).
 * Se centra en la estrategia y ejecución de turnos.
 */
@Service
@Transactional
@Slf4j
public class BotServiceImpl implements BotService {

    private final BotStrategyFactory botStrategyFactory;
    private final BotProfileRepository botProfileRepository;

    @Autowired
    public BotServiceImpl(BotStrategyFactory botStrategyFactory, BotProfileRepository botProfileRepository) {
        this.botStrategyFactory = botStrategyFactory;
        this.botProfileRepository = botProfileRepository;
    }

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameStateService gameStateService;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private PlayerMapper playerMapper;
    /**
     * Ejecuta el turno completo de un bot: Refuerzo -> Ataque -> Fortificación.
     * Los bots siempre ejecutan todas las fases automáticamente.
     *
     * @param botPlayer El bot que ejecuta el turno
     * @param game El juego actual
     */
    @Override
    public void executeBotTurn(PlayerEntity botPlayer, GameEntity game) {
        if (botPlayer.getBotProfile() == null) {
            throw new IllegalStateException("Player is not a bot: " + botPlayer.getId());
        }

        log.info("Executing complete turn for bot: {} (Level: {}, Strategy: {})",
                botPlayer.getBotProfile().getBotName(),
                botPlayer.getBotProfile().getLevel(),
                botPlayer.getBotProfile().getStrategy());

        try {
            BotStrategyExecutor executor = botStrategyFactory.getExecutor(botPlayer.getBotProfile());

            // Ejecutar el turno completo (las 3 fases)
            executor.executeTurn(botPlayer, game);

            log.info("Bot {} successfully completed turn in game {}",
                    botPlayer.getBotProfile().getBotName(), game.getGameCode());

        } catch (Exception e) {
            log.error("Error executing turn for bot {}: {}",
                    botPlayer.getBotProfile().getBotName(), e.getMessage());
            throw new RuntimeException("Failed to execute bot turn", e);
        }
    }

    // ===============================
    // ANÁLISIS Y ESTRATEGIA
    // ===============================

    /**
     * Evalúa la probabilidad de éxito de un ataque basado en la estrategia del bot.
     *
     * @param botPlayer El bot que evalúa
     * @param attackerArmies Ejércitos atacantes
     * @param defenderArmies Ejércitos defensores
     * @return Probabilidad de éxito (0.0 a 1.0)
     */
    @Override
    public double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies) {
        if (botPlayer.getBotProfile() == null) {
            throw new IllegalStateException("Player is not a bot: " + botPlayer.getId());
        }

        BotStrategyExecutor executor = botStrategyFactory.getExecutor(botPlayer.getBotProfile());
        return executor.evaluateAttackProbability(botPlayer, attackerArmies, defenderArmies);
    }

    /**
     * Obtiene los mejores objetivos de ataque según la estrategia del bot.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return Lista de países ordenados por prioridad de ataque
     */
    @Override
    public List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game) {
        if (botPlayer.getBotProfile() == null) {
            throw new IllegalStateException("Player is not a bot: " + botPlayer.getId());
        }

        BotStrategyExecutor executor = botStrategyFactory.getExecutor(botPlayer.getBotProfile());
        return executor.getBestAttackTargets(botPlayer, game);
    }

    /**
     * Obtiene las mejores posiciones defensivas según la estrategia del bot.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return Lista de países ordenados por prioridad defensiva
     */
    @Override
    public List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game) {
        if (botPlayer.getBotProfile() == null) {
            throw new IllegalStateException("Player is not a bot: " + botPlayer.getId());
        }

        BotStrategyExecutor executor = botStrategyFactory.getExecutor(botPlayer.getBotProfile());
        return executor.getBestDefensePositions(botPlayer, game);
    }

    /**
     * Verifica si un bot debería atacar en la situación actual.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return true si debería atacar, false si no
     */
    @Override
    public boolean shouldBotAttack(PlayerEntity botPlayer, GameEntity game) {
        if (botPlayer.getBotProfile() == null) {
            return false;
        }

        BotStrategyExecutor executor = botStrategyFactory.getExecutor(botPlayer.getBotProfile());
        List<CountryEntity> targets = executor.getBestAttackTargets(botPlayer, game);

        // Si no hay objetivos viables, no atacar
        if (targets.isEmpty()) {
            return false;
        }

        // Evaluar si el mejor objetivo vale la pena
        CountryEntity bestTarget = targets.get(0);
        // Aquí podrías agregar lógica más sofisticada basada en la estrategia

        return true; // Por ahora, si hay objetivos, atacar
    }

    /**
     * Verifica si un bot debería fortificar en la situación actual.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return true si debería fortificar, false si no
     */
    @Override
    public boolean shouldBotFortify(PlayerEntity botPlayer, GameEntity game) {
        if (botPlayer.getBotProfile() == null) {
            return false;
        }

        // Los bots siempre intentan fortificar si es posible
        // La lógica específica está en cada estrategia
        return true;
    }

    // ===============================
    // CRUD DE BOT PROFILES
    // ===============================

    @Override
    public BotProfileEntity save(BotProfileEntity botProfile) {
        return botProfileRepository.save(botProfile);
    }

    @Override
    public Optional<BotProfileEntity> findById(Long id) {
        return botProfileRepository.findById(id);
    }

    @Override
    public List<BotProfileEntity> findAll() {
        return botProfileRepository.findAll();
    }

    @Override
    public List<BotProfileEntity> findByLevel(BotLevel level) {
        return botProfileRepository.findByLevel(level);
    }

    @Override
    public void deleteById(Long id) {
        botProfileRepository.deleteById(id);
    }

    // ===============================
    // UTILIDADES
    // ===============================

    /**
     * Valida que un player sea efectivamente un bot.
     *
     * @param player El player a validar
     * @return true si es un bot válido
     */
    public boolean isValidBot(PlayerEntity player) {
        return player != null &&
                player.getBotProfile() != null &&
                player.getBotProfile().getBotName() != null;
    }

    /**
     * Obtiene el nombre descriptivo del bot.
     *
     * @param botPlayer El bot
     * @return Nombre descriptivo del bot
     */
    public String getBotDisplayName(PlayerEntity botPlayer) {
        if (!isValidBot(botPlayer)) {
            return "Unknown Bot";
        }

        return String.format("%s (%s - %s)",
                botPlayer.getBotProfile().getBotName(),
                botPlayer.getBotProfile().getLevel(),
                botPlayer.getBotProfile().getStrategy());
    }

    @Override
    public GameResponseDto executeBotTurnComplete(String gameCode, Long botId) {
        Optional<Player> botPlayerOpt = playerService.findById(botId);
        if (botPlayerOpt.isEmpty() || !botPlayerOpt.get().getIsBot()) {
            throw new IllegalArgumentException("Bot not found or not a bot with ID: " + botId);
        }

        Player botPlayer = botPlayerOpt.get();
        Game game = gameService.findByGameCode(gameCode);

        if (!gameStateService.isPlayerTurn(game, botId)) {
            throw new IllegalStateException("It's not bot's turn. Bot: " + botId + ", Current player: " + game.getCurrentPlayerIndex());
        }

//        if (!gameStateService.canPerformAction(game, "bot_turn")) {
//            throw new IllegalStateException("Game not in valid state for bot turn execution: " + game.getState());
//        }

        PlayerEntity botEntity = playerMapper.toEntity(botPlayer);
        GameEntity gameEntity = gameMapper.toEntity(game);

        executeBotTurn(botEntity, gameEntity);

        //GameResponseDto refreshGame = gameService.getGameByCode(gameCode);

        gameStateService.nextTurn(game);
        Game savedGame = gameService.save(game);

        return gameMapper.toResponseDto(savedGame);
    }
}