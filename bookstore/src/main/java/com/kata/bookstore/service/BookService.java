package com.kata.bookstore.service;

import com.kata.bookstore.entity.Book;
import com.kata.bookstore.exception.ResourceNotFoundException;
import com.kata.bookstore.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book addBook(Book book) {
        return bookRepository.findByTitleAndAuthor(
                book.getTitle(),
                book.getAuthor()
        ).map(existingBook -> {
            existingBook.setStock(existingBook.getStock() + book.getStock());
            return bookRepository.save(existingBook);
        }).orElseGet(() -> bookRepository.save(book));
    }

    public Book updateBook(long id, Book book) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException(
                            "Book not found with id: " + id);
                });
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setStock(book.getStock());
        return bookRepository.save(existingBook);
    }

    public Book getBookById(long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
}
