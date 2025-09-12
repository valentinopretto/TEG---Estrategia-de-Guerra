package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.service.impl.InitialPlacementServiceImpl;

import java.util.Map;

public interface InitialPlacementService {
    void placeInitialArmies(String gameCode, Long playerId, Map<Long, Integer> armiesByCountry);
    InitialPlacementServiceImpl.PlayerInitialStatus getPlayerStatus(String gameCode, Long playerId);

}
