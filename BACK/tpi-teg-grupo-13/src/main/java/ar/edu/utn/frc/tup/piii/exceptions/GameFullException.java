package ar.edu.utn.frc.tup.piii.exceptions;

/**
        * Excepción lanzada cuando se intenta unir a una partida que ya está llena
 */
public class GameFullException extends RuntimeException {

  public GameFullException(String message) {
    super(message);
  }

  public GameFullException(String message, Throwable cause) {
    super(message, cause);
  }

  public GameFullException(String gameCode, int currentPlayers, int maxPlayers) {
    super(String.format("La partida %s está llena (%d/%d jugadores)", gameCode, currentPlayers, maxPlayers));
  }
}
