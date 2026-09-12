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
        Book book = new Book();
        book.setTitle("Book1");
        book.setAuthor("Author Name1");
        book.setPrice(new BigDecimal("500"));
        book.setStock(10);
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
}
