package com.kata.bookstore.service;

import com.kata.bookstore.entity.Book;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    public Book addBook(Book book) {
        return book;
    }
}
