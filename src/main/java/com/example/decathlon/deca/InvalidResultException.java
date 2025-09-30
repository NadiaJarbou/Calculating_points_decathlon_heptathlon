package com.example.decathlon.deca;

//A new Exception
public class InvalidResultException extends Exception {
    public InvalidResultException(String message) {
        super(message);
    }
}