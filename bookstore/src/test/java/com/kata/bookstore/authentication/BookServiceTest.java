package com.kata.bookstore.authentication;

import com.kata.bookstore.entity.Book;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookService bookService;

    @Test
    public void addNewBook() {
        Book book =  Book.builder().id(1l).title("Book Name").author("Author Name")
                .price(new BigDecimal("500.00")).stock(5).build();
        when(bookRepository.findByTitleAndAuthor("Book1","Author Name1")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Book result = bookService.addBook(book);
        // assert
        assertNotNull(result);
        assertEquals("Book1", result.getTitle());
        assertEquals("Author Name1", result.getAuthor());
        assertEquals(new BigDecimal("500"), result.getPrice());
        assertEquals(10, result.getStock());
        verify(bookRepository).save(any(Book.class));
    }
    @Test
    void shouldIncrementStockWhenAdminAddsExistingBook() {
        Book existingBook = Book.builder().id(1l).title("Book Name").author("Author Name")
                .price(new BigDecimal("500.00")).stock(10).build();
        Book newBook = Book.builder().id(1l).title("Book Name").author("Author Name")
                .price(new BigDecimal("500.00")).stock(5).build();
        when(bookRepository.findByTitleAndAuthor("Book Name","Author Name"))
                .thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Book result = bookService.addBook(newBook);
        assertNotNull(result);
        assertEquals(15, result.getStock());
        verify(bookRepository).save(existingBook);
    }
}
