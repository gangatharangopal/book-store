package com.kata.bookstore.authentication;

 import com.kata.bookstore.entity.Book;
 import com.kata.bookstore.entity.Cart;
 import com.kata.bookstore.entity.CartItem;
 import com.kata.bookstore.entity.User;
 import com.kata.bookstore.repository.CartRepository;
 import com.kata.bookstore.service.CartService;
 import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

 import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void shouldAddBookToCart() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book Name1").author("Author1")
                .price(new BigDecimal("500.00")).stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Cart result = cartService.addBookToCart(user, book, 2);
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        CartItem cartItem = result.getItems().get(0);
        assertEquals(book, cartItem.getBook());
        assertEquals(2, cartItem.getQuantity());
        assertEquals(cart, cartItem.getCart());
        verify(cartRepository).findByUser(user);
        verify(cartRepository).save(cart);
    }
}