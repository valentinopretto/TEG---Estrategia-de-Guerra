package ar.edu.utn.frc.tup.piii.exceptions;

public class InvalidGameConfigurationException extends RuntimeException {

    public InvalidGameConfigurationException(String message) {
        super(message);
    }

    public InvalidGameConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
