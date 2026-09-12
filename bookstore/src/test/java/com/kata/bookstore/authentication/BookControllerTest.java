package com.kata.bookstore.authentication;

import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.kata.bookstore.entity.Book;
import com.kata.bookstore.exception.ResourceNotFoundException;
import com.kata.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private BookService bookService;
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
}

