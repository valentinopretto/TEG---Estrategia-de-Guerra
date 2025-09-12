package ar.edu.utn.frc.tup.piii.FactoryBots;

import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;

import java.util.List;

public interface BotStrategyExecutor {
    BotLevel getLevel();

    BotStrategy getStrategy();

    void executeTurn(PlayerEntity botPlayer, GameEntity game);

    void performBotReinforcement(PlayerEntity botPlayer, GameEntity game);

    void performBotAttack(PlayerEntity botPlayer, GameEntity game);

    void performBotFortify(PlayerEntity botPlayer, GameEntity game);

    double evaluateAttackProbability(PlayerEntity botPlayer,int attackerArmies, int defenderArmies);

    List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game);

    List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game);

}
