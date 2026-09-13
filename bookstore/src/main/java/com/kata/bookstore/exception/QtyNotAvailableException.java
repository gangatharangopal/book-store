package com.kata.bookstore.exception;

public class QtyNotAvailableException extends RuntimeException {
    public QtyNotAvailableException(String message) {
        super(message);
    }
}