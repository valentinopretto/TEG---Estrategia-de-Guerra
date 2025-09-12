package ar.edu.utn.frc.tup.piii.exceptions;

/**
 * Excepción base para errores relacionados con el estado del juego
 */
public class GameStateException extends RuntimeException {

    public GameStateException(String message) {
        super(message);
    }

    public GameStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
