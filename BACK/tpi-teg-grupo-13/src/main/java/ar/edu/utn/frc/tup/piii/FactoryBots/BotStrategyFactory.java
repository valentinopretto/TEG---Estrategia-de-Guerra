package ar.edu.utn.frc.tup.piii.FactoryBots;
import ar.edu.utn.frc.tup.piii.FactoryBots.BalancedStrategies.BalancedAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.FactoryBots.ExpertStrategies.ExpertAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.FactoryBots.NoviceStrategies.NoviceAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;


@Component
public class BotStrategyFactory {

    private final Map<String, BotStrategyExecutor> executorMap = new HashMap<>();

    @Autowired
    public BotStrategyFactory(
            NoviceAggressiveExecutor noviceAggressive,

            BalancedAggressiveExecutor balancedAggressive,  //3 bots

            ExpertAggressiveExecutor expertAggressive
    ) {
        executorMap.put(key(BotLevel.NOVICE, BotStrategy.AGGRESSIVE), noviceAggressive);

        executorMap.put(key(BotLevel.BALANCED, BotStrategy.AGGRESSIVE), balancedAggressive);

        executorMap.put(key(BotLevel.EXPERT, BotStrategy.AGGRESSIVE), expertAggressive);
    }

    public BotStrategyExecutor getExecutor(BotProfileEntity profile) {
        if (profile == null || profile.getLevel() == null || profile.getStrategy() == null) {
            throw new IllegalArgumentException("BotProfile inválido: faltan level o strategy");
        }

        String key = key(profile.getLevel(), profile.getStrategy());
        BotStrategyExecutor executor = executorMap.get(key);

        if (executor == null) {
            throw new IllegalArgumentException("No se encontró un executor para: " +
                    profile.getLevel() + " - " + profile.getStrategy());
        }

        return executor;
    }

    private String key(BotLevel level, BotStrategy strategy) {
        return level.name() + "_" + strategy.name();
    }
}


