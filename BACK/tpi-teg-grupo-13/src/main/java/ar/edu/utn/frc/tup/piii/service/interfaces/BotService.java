package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión y ejecución de lógica de bots.
 * Se enfoca en la estrategia y ejecución de turnos, no en el CRUD de players.
 */
public interface BotService {

    /**
     * Ejecuta el turno completo de un bot (todas las fases automáticamente).
     *
     * @param botPlayer El bot que ejecuta el turno
     * @param game El juego actual
     */
    void executeBotTurn(PlayerEntity botPlayer, GameEntity game);

    /**
     * Evalúa la probabilidad de éxito de un ataque.
     *
     * @param botPlayer El bot que evalúa
     * @param attackerArmies Ejércitos atacantes
     * @param defenderArmies Ejércitos defensores
     * @return Probabilidad de éxito (0.0 a 1.0)
     */
    double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies);

    /**
     * Obtiene los mejores objetivos de ataque según la estrategia del bot.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return Lista de países ordenados por prioridad de ataque
     */
    List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game);

    /**
     * Obtiene las mejores posiciones defensivas según la estrategia del bot.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return Lista de países ordenados por prioridad defensiva
     */
    List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game);

    /**
     * Determina si un bot debería atacar en la situación actual.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return true si debería atacar
     */
    boolean shouldBotAttack(PlayerEntity botPlayer, GameEntity game);

    /**
     * Determina si un bot debería fortificar en la situación actual.
     *
     * @param botPlayer El bot que evalúa
     * @param game El juego actual
     * @return true si debería fortificar
     */
    boolean shouldBotFortify(PlayerEntity botPlayer, GameEntity game);

    /**
     * Guarda un perfil de bot.
     *
     * @param botProfile El perfil a guardar
     * @return El perfil guardado
     */
    BotProfileEntity save(BotProfileEntity botProfile);

    /**
     * Busca un perfil de bot por ID.
     *
     * @param id ID del perfil
     * @return El perfil si existe
     */
    Optional<BotProfileEntity> findById(Long id);

    /**
     * Obtiene todos los perfiles de bots.
     *
     * @return Lista de todos los perfiles
     */
    List<BotProfileEntity> findAll();

    /**
     * Busca perfiles de bots por nivel.
     *
     * @param level Nivel de dificultad
     * @return Lista de perfiles del nivel especificado
     */
    List<BotProfileEntity> findByLevel(BotLevel level);

    /**
     * Elimina un perfil de bot por ID.
     *
     * @param id ID del perfil a eliminar
     */
    void deleteById(Long id);

    /**
     * Valida que un player sea efectivamente un bot.
     *
     * @param player El player a validar
     * @return true si es un bot válido
     */
    boolean isValidBot(PlayerEntity player);

    /**
     * Obtiene el nombre descriptivo del bot.
     *
     * @param botPlayer El bot
     * @return Nombre descriptivo del bot
     */
    String getBotDisplayName(PlayerEntity botPlayer);

    GameResponseDto executeBotTurnComplete(String gameCode, Long botId);
}