package ar.edu.utn.frc.tup.piii.exceptions;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException (String msg){
        super(msg);
    }
}
