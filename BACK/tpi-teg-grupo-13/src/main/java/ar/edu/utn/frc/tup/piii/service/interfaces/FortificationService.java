package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import java.util.List;

/**
 * Servicio para manejar la fortificación de territorios en el TEG.
 * La fortificación permite mover ejércitos entre territorios propios conectados.
 */
public interface FortificationService {

    /**
     * Ejecuta una fortificación moviendo ejércitos entre dos territorios propios.
     *
     * @param gameCode código del juego
     * @param fortifyDto datos de la fortificación
     * @return true si la fortificación fue exitosa
     */
    boolean performFortification(String gameCode, FortifyDto fortifyDto);

    /**
     * Obtiene todos los territorios que un jugador puede usar como origen para fortificar.
     * (territorios con más de 1 ejército)
     *
     * @param gameCode código del juego
     * @param playerId ID del jugador
     * @return lista de territorios que pueden ser origen de fortificación
     */
    List<Territory> getFortifiableTerritoriesForPlayer(String gameCode, Long playerId);

    /**
     * Obtiene todos los territorios que pueden ser destino de fortificación desde un territorio específico.
     * (territorios propios conectados por una cadena de territorios propios)
     *
     * @param gameCode código del juego
     * @param fromTerritoryId ID del territorio origen
     * @param playerId ID del jugador (para verificar propiedad)
     * @return lista de territorios que pueden recibir ejércitos
     */
    List<Territory> getFortificationTargetsForTerritory(String gameCode, Long fromTerritoryId, Long playerId);

    /**
     * Verifica si dos territorios están conectados por una cadena de territorios del mismo jugador.
     * Utiliza algoritmo de búsqueda para encontrar un camino válido.
     *
     * @param gameCode código del juego
     * @param fromTerritoryId territorio origen
     * @param toTerritoryId territorio destino
     * @param playerId ID del jugador propietario
     * @return true si existe un camino válido entre los territorios
     */
    boolean areTerritoriesConnectedByPlayer(String gameCode, Long fromTerritoryId, Long toTerritoryId, Long playerId);

    /**
     * Valida si una fortificación es legal según las reglas del TEG.
     *
     * @param gameCode código del juego
     * @param fortifyDto datos de la fortificación a validar
     * @return true si la fortificación es válida
     */
    boolean isValidFortification(String gameCode, FortifyDto fortifyDto);

    /**
     * Calcula el máximo número de ejércitos que se pueden mover desde un territorio.
     * (total - 1, ya que debe quedar al menos 1 ejército)
     *
     * @param gameCode código del juego
     * @param territoryId ID del territorio
     * @return número máximo de ejércitos que se pueden mover
     */
    int getMaxMovableArmies(String gameCode, Long territoryId);
}