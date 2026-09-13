package com.kata.bookstore.controller;

import com.kata.bookstore.dto.AddToCartRequest;
import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.CartItem;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.repository.UserRepository;
import com.kata.bookstore.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CartService cartService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Test
    @WithMockUser(username = "user1", roles = "User")
    void shouldCheckoutCartSuccessfully() throws Exception {
        BookOrder order = BookOrder.builder().build();
        when(cartService.checkout()).thenReturn(order);
        mockMvc.perform(post("/api/cart/checkout").with(httpBasic("user1", "1234")))
                .andExpect(status().isCreated());
        verify(cartService).checkout();
    }

    @Test
    @WithMockUser(username = "user1", roles = "User")
    void addBookToCartTest() throws Exception {
        User user = User.builder().id(1L).username("user1").build();
        Book book = Book.builder().id(1L).title("Book Name1").author("Author1")
                .price(new BigDecimal("500.00")).stock(10).build();
        AddToCartRequest request = AddToCartRequest.builder().bookId(1L).quantity(2).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(cartService.addBookToCart(request))
                .thenReturn(Cart.builder().id(1L).user(user).build());
        mockMvc.perform(post("/api/cart").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "bookId": 1,
                                "quantity": 2
                            }
                            """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "User")
    void shouldReturnUserCart() throws Exception {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book Name1").author("Author1")
                .price(new BigDecimal("500.00")).stock(10)
                .build();
        CartItem cartItem = CartItem.builder().id(1L).book(book).quantity(2).build();
        Cart cart = Cart.builder().id(1L)
                .user(user)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();
        when(cartService.getUserCart()).thenReturn(cart);
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.username").value("user"))
                .andExpect(jsonPath("$.items[0].book.id").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(cartService).getUserCart();
    }
    @Test
    @WithMockUser(username = "user", roles = "User")
    void shouldRemoveBookFromCart() throws Exception {

        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L)
                .title("Book Name1").author("Author1").price(new BigDecimal("500.00"))
                .stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        when(cartService.removeBookFromCart(1L)).thenReturn(cart);
        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.username").value("user"))
                .andExpect(jsonPath("$.items").isEmpty());
        verify(cartService).removeBookFromCart(1L);
    }
}