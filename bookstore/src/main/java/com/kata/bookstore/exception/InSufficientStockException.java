package com.kata.bookstore.exception;

public class InSufficientStockException extends RuntimeException {
    public InSufficientStockException(String message) {
        super(message);
    }
}
