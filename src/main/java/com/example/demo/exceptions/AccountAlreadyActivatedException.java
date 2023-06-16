package com.example.demo.exceptions;

public class AccountAlreadyActivatedException extends RuntimeException{

    public AccountAlreadyActivatedException (String message){
        super(message);
    }

}
