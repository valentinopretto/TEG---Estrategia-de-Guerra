package ar.edu.utn.frc.tup.piii.exceptions;

/**
 * Excepción lanzada cuando no hay colores disponibles para asignar a un jugador
 */
public class ColorNotAvailableException extends RuntimeException {

  public ColorNotAvailableException(String message) {
    super(message);
  }

  public ColorNotAvailableException(String message, Throwable cause) {
    super(message, cause);
  }

}