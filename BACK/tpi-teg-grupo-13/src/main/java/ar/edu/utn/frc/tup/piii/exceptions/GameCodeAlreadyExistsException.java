package ar.edu.utn.frc.tup.piii.exceptions;

/**
 * Excepción lanzada cuando se intenta crear un juego con un código que ya existe
 */
public class GameCodeAlreadyExistsException extends RuntimeException {

    public GameCodeAlreadyExistsException(String message) {
        super(message);
    }

    public GameCodeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
