package ar.edu.utn.frc.tup.piii.exceptions;

import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;

/**
 * Excepción lanzada cuando no se encuentra un perfil de bot específico
 */
public class BotProfileNotFoundException extends RuntimeException {

    public BotProfileNotFoundException(String message) {
        super(message);
    }

    public BotProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BotProfileNotFoundException(BotLevel level, BotStrategy strategy) {
        super(String.format("No se encontró bot con nivel %s y estrategia %s", level, strategy));
    }
}
