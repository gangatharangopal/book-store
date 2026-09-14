package com.kata.bookstore.Service;

import com.kata.bookstore.entity.Book;
import com.kata.bookstore.exception.ResourceNotFoundException;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.service.BookService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookService bookService;

    @Test
    public void addNewBook() {
        Book book =  Book.builder().id(1l).title("Book Name").author("Author Name")
                .price(new BigDecimal("500.00")).stock(10).build();
        when(bookRepository.findByTitleAndAuthor("Book Name","Author Name")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Book result = bookService.addBook(book);
        assertNotNull(result);
        assertEquals("Book Name", result.getTitle());
        assertEquals("Author Name", result.getAuthor());
        assertEquals(new BigDecimal("500.00"), result.getPrice());
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
    @Test
    void shouldNotCreateDuplicateBook() {
        Book existingBook = Book.builder().id(1L).title("Book Name").author("Author Name").price(new BigDecimal("500.00")).stock(10)
                            .build();
        Book newBook = Book.builder()
                .title("Book Name").author("Author Name").price(new BigDecimal("500.00")).stock(5)
                .build();
        when(bookRepository.findByTitleAndAuthor("Book Name","Author Name")).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Book result = bookService.addBook(newBook);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(15, result.getStock());
        verify(bookRepository).save(existingBook);
        verify(bookRepository, never()).save(newBook);
    }

    @Test
    void shouldReturnAllBooks() {
        Book book1 = Book.builder().id(1L).title("Book Name1").author("Author Name1")
                .price(new BigDecimal("500.00")).stock(10).build();
        Book book2 =Book.builder().id(2L).title("Book Name2").author("Author Name1")
                .price(new BigDecimal("500.00")).stock(15).build();
        Pageable pageable = PageRequest.of(0,10, Sort.by("title").ascending());
        Page<Book> bookPage = new PageImpl<>(List.of(book1, book2),pageable,2);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        Page<Book> result = bookService.getAllBooks(pageable);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(2, result.getContent().size());
        Assertions.assertEquals("Book Name1", result.getContent().get(0).getTitle());
        Assertions.assertEquals("Book Name2", result.getContent().get(1).getTitle());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void addExistingBook() {
        Book existingBook = Book.builder().id(1L).title("Book Name").author("Author Name")
                .price(new BigDecimal("500.00")).stock(10).build();
        Book book = Book.builder().id(2L).title("Book Name").author("Author Name")
                .price(new BigDecimal("500.00")).stock(5).build();
        when(bookRepository.findByTitleAndAuthor("Book Name", "Author Name")).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Book result = bookService.addBook(book);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Book Name", result.getTitle());
        assertEquals("Author Name", result.getAuthor());
        assertEquals(new BigDecimal("500.00"), result.getPrice());
        assertEquals(15, result.getStock());
        verify(bookRepository).findByTitleAndAuthor("Book Name", "Author Name");
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBook() {
        Book existingBook = Book.builder().id(1L).title("Old Title").author("Old Author")
                .price(new BigDecimal("400.00")).stock(10).build();
        Book updatedBook = Book.builder().title("New Title").author("New Author")
                .price(new BigDecimal("500.00")).stock(20).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Book result = bookService.updateBook(1L, updatedBook);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Title", result.getTitle());
        assertEquals("New Author", result.getAuthor());
        assertEquals(new BigDecimal("500.00"), result.getPrice());
        assertEquals(20, result.getStock());
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBookShouldThrowExceptionWhenBookNotFound() {
        Book updatedBook = Book.builder().title("New Title").author("New Author")
                .price(new BigDecimal("500.00")).stock(20).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.updateBook(1L, updatedBook)
        );
        assertEquals("Book not found with id: 1",exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }
    @Test
    void getBookById() {
        Book book = Book.builder().id(1L).title("Book Name")
                .author("Author Name").price(new BigDecimal("500.00")).stock(10)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        Book result = bookService.getBookById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10, result.getStock());
        verify(bookRepository).findById(1L);
    }
    @Test
    void getBookByIdShouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.getBookById(1L)
        );
        assertEquals("Book not found with id: 1",exception.getMessage());
        verify(bookRepository).findById(1L);
    }
    @Test
    void getAllBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Book book1 = Book.builder().id(1L).title("Book Name1")
                .author("Author1").price(new BigDecimal("500.00")).stock(10)
                .build();
        Book book2 = Book.builder().id(2L).title("Book Name2")
                .author("Author2").price(new BigDecimal("600.00")).stock(20)
                .build();
        Page<Book> bookPage = new PageImpl<>(List.of(book1, book2),pageable,2);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        Page<Book> result = bookService.getAllBooks(pageable);
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Book Name2",result.getContent().get(1).getTitle());
        verify(bookRepository).findAll(pageable);
    }
    @Test
    void shouldGetBookById() {

        Book book = Book.builder()
                .id(1L)
                .title("Book Name")
                .author("Author Name")
                .price(new BigDecimal("500.00"))
                .stock(10)
                .build();

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Book Name", result.getTitle());

        verify(bookRepository).findById(1L);
    }

}
