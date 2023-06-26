package com.example.demo.exceptions;

public class RiderUnavailableException extends RuntimeException{

    public RiderUnavailableException(String message) {
        super(message);
    }
}
