package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import java.util.List;

public interface CombatService {

    CombatResultDto performCombat(String gameCode, AttackDto attackDto);

    List<Territory> getAttackableTerritoriesForPlayer(String gameCode, Long playerId);

    List<Territory> getTargetsForTerritory(String gameCode, Long territoryId, Long playerId);

    CombatResultDto performCombatWithValidation(String gameCode, AttackDto attackDto);
}