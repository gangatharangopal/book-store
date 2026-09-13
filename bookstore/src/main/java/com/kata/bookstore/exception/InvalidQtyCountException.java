package com.kata.bookstore.exception;

public class InvalidQtyCountException extends RuntimeException {
    public InvalidQtyCountException(String message) {
        super(message);
    }
}
