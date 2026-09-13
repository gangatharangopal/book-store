package com.kata.bookstore.controller;

import com.kata.bookstore.entity.Book;
import com.kata.bookstore.exception.ResourceNotFoundException;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.service.BookService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private BookService bookService;
    @Mock
    private BookRepository bookRepository;
    @Test
    @WithMockUser(username = "admin", roles = "Admin")
    public void adminOnlyTest() throws Exception {
        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Book Name",
                                    "author": "Author Name",
                                    "price": 500,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isCreated());
    }
    @Test
    @WithMockUser(username = "user", roles = "User")
    public void userBookAddFailCase() throws Exception {
        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Book Name",
                                    "author": "Author Name",
                                    "price": 500,
                                    "stock": 10
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "Admin")
    void adminCanUpdateBook() throws Exception {
        Book updatedBook = Book.builder().id(1L)
                .title("New Book Name").author("New Author").price(new BigDecimal("500.00"))
                .stock(20)
                .build();

        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(updatedBook);
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "New Book Name",
                                "author": "New Author",
                                "price": 500.00,
                                "stock": 20
                            }
                            """))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "admin", roles = "Admin")
    void shouldReturn404WhenUpdatingNonExistingBook() throws Exception {
        when(bookService.updateBook(eq(19L), any(Book.class)))
                .thenThrow(new ResourceNotFoundException("Book not found with id: 19"));
        mockMvc.perform(put("/api/books/19")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "New Book Name",
                                "author": "New Author",
                                "price": 500.00,
                                "stock": 20
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "Admin")
    void shouldReturn400ForInvalidBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "",
                                "author": "Author Name",
                                "price": 500.00,
                                "stock": 10
                            }
                            """))
                .andExpect(status().isBadRequest());
        verify(bookService, never()).addBook(any(Book.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "Admin")
    void shouldReturn404ForNonExistingBook() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new ResourceNotFoundException(
                        "Book not found with id: 99"));
        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllBooks() {
        Book book1 = Book.builder().id(1L).title("Book Name1").author("Author Name1")
                .price(new BigDecimal("500.00")).stock(10).build();
        Book book2 =Book.builder().id(2L).title("Book Name2").author("Author Name1")
                .price(new BigDecimal("500.00")).stock(15).build();

        Pageable pageable = PageRequest.of(0,10,Sort.by("title").ascending());
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
    @WithMockUser(username = "user", roles = "User")
    void shouldReturnEmptyListWhenNoBooksExist() throws Exception {
        Pageable pageable = PageRequest.of(0,10,Sort.by("title").ascending());
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(Page.empty(pageable));
        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}

