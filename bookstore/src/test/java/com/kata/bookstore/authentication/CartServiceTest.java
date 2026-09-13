package com.kata.bookstore.authentication;

import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.CartItem;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.exception.QtyNotAvailableException;
import com.kata.bookstore.repository.CartRepository;
import com.kata.bookstore.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void increaseQuantityWhenSameBookAddedAgainTest() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Clean Code").author("Robert C. Martin").price(new BigDecimal("500.00")).stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem existingItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.getItems().add(existingItem);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Cart result = cartService.addBookToCart(user, book, 3);
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        CartItem cartItem = result.getItems().get(0);
        assertEquals(book, cartItem.getBook());
        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository).findByUser(user);
        verify(cartRepository).save(cart);
    }

    @Test
    void validateNotToAddWhenStockZero() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book name1").author("Author name").price(new BigDecimal("500.00")).stock(0).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        assertThrows(IllegalStateException.class,() -> cartService.addBookToCart(user, book, 1));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void validateNotToAddWhenStockLess() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book name1").author("Author name").price(new BigDecimal("500.00")).stock(0).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        assertThrows(IllegalStateException.class,() -> cartService.addBookToCart(user, book, 1));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
   public void shouldNotAddMoreThanAvailableStock() {
        User user = User.builder().id(1L).username("user").build();
        // 5 available
        Book book = Book.builder().id(1L).title("Book name1").author("Author name")
                    .price(new BigDecimal("500.00")).stock(5).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        // 3 in the card
        CartItem existingItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(3).build();
        cart.getItems().add(existingItem);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        assertThrows(QtyNotAvailableException.class,() -> cartService.addBookToCart(user, book, 3));
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    public void returnUserCartTest(){
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book name1").author("Author name").price(new BigDecimal("500.00")).stock(5).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.getItems().add(cartItem);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        Cart result = cartService.getUserCart(user);
        assertNotNull(result);
        assertEquals(cart, result);
        assertEquals(1, result.getItems().size());
        assertEquals(book, result.getItems().get(0).getBook());
        assertEquals(2, result.getItems().get(0).getQuantity());
        verify(cartRepository).findByUser(user);
    }

    @Test
    void updateCartItemQtyTest() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book name1").author("Author name").price(new BigDecimal("500.00"))
                    .stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.getItems().add(cartItem);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.updateCartItemQuantity(user, book.getId(), 5);
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    @Test
    void removeBookFromCart() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book name1").author("Author name")
                    .price(new BigDecimal("500.00")).stock(5).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.getItems().add(cartItem);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Cart result = cartService.removeBookFromCart(user, book.getId());
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());

        verify(cartRepository).findByUser(user);
        verify(cartRepository).save(cart);
    }
}