package ar.edu.utn.frc.tup.piii.exceptions;


public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException (String msg){
        super(msg);
    }
}
