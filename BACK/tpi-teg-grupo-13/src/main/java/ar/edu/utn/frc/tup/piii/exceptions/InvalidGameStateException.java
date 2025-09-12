package ar.edu.utn.frc.tup.piii.exceptions;

import ar.edu.utn.frc.tup.piii.model.enums.GameState;

/**
 * Excepción lanzada cuando se intenta realizar una operación en un estado de juego inválido
 */
public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException(String message) {
        super(message);
    }

    public InvalidGameStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGameStateException(String gameCode, GameState currentState, GameState expectedState) {
        super(String.format("La partida %s está en estado %s, se esperaba %s",
                gameCode, currentState, expectedState));
    }

    public InvalidGameStateException(String gameCode, GameState currentState, String operation) {
        super(String.format("No se puede %s en la partida %s porque está en estado %s",
                operation, gameCode, currentState));
    }
}