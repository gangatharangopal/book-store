package com.kata.bookstore.service;

import com.kata.bookstore.entity.Book;
import com.kata.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book addBook(Book book) {
        Optional<Book> existingBook = bookRepository.findByTitleAndAuthor(book.getTitle(),book.getAuthor());
        return existingBook.orElseGet(() -> bookRepository.save(book));
    }
}
