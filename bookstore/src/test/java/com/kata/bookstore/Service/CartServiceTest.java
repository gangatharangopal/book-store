package com.kata.bookstore.Service;

import com.kata.bookstore.dto.AddToCartRequest;
import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.CartItem;
import com.kata.bookstore.entity.OrderItem;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.exception.InSufficientStockException;
import com.kata.bookstore.exception.QtyNotAvailableException;
import com.kata.bookstore.repository.BookOrderRepository;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.repository.CartRepository;
import com.kata.bookstore.repository.UserRepository;
import com.kata.bookstore.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @InjectMocks
    private CartService cartService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookOrderRepository bookOrderRepository;

    @Test
    void shouldAddBookToCart() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book Name1").author("Author1")
                .price(new BigDecimal("500.00")).stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        AddToCartRequest request = AddToCartRequest.builder().bookId(1l).quantity(2).build();
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Cart result = cartService.addBookToCart(request);
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
        Book book = Book.builder().id(1L).title("Book name1").author("Author1").price(new BigDecimal("500.00")).stock(10).build();
        AddToCartRequest request = AddToCartRequest.builder().bookId(1l).quantity(2).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem existingItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.getItems().add(existingItem);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Cart result = cartService.addBookToCart(request);
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        CartItem cartItem = result.getItems().get(0);
        assertEquals(book, cartItem.getBook());
        assertEquals(4, cartItem.getQuantity());
        verify(cartRepository).findByUser(user);
        verify(cartRepository).save(cart);
    }

    @Test
    void validateNotToAddWhenStockZero() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book name1").author("Author name").price(new BigDecimal("500.00")).stock(0).build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        AddToCartRequest request = AddToCartRequest.builder().bookId(1l).quantity(2).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        assertThrows(IllegalStateException.class,() -> cartService.addBookToCart(request));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void validateNotToAddWhenStockLess() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Book name1").author("Author name").price(new BigDecimal("500.00")).stock(0).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        AddToCartRequest request = AddToCartRequest.builder().bookId(1l).quantity(2).build();
        assertThrows(IllegalStateException.class,() -> cartService.addBookToCart(request));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
   public void shouldNotAddMoreThanAvailableStock() {
        User user = User.builder().id(1L).username("user1").build();
        // 5 available
        Book book = Book.builder().id(1L).title("Book name1").author("Author name")
                    .price(new BigDecimal("500.00")).stock(2).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        AddToCartRequest request = AddToCartRequest.builder().bookId(1l).quantity(3).build();
        // 3 in the cart
        CartItem existingItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(3).build();
        cart.getItems().add(existingItem);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user1");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThrows(QtyNotAvailableException.class,() -> cartService.addBookToCart(request));
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
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Cart result = cartService.getUserCart();
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
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Cart result = cartService.removeBookFromCart(book.getId());
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        verify(cartRepository).findByUser(user);
        verify(cartRepository).save(cart);
    }

    @Test
    void onCheckoutGetUserCartTestor() {
        User user = User.builder().id(1L).username("user").build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user",null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        assertThrows(IllegalStateException.class, () -> cartService.checkout());
        verify(cartRepository).findByUser(user);
    }

    @Test
    void getUserCartOnCheckOut() {
        User user = User.builder().id(1L).username("user").build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        Book book = Book.builder().id(1L).title("Clean Code").price(new BigDecimal("500")).stock(10).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.setItems(new ArrayList<>(List.of(cartItem)));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        cartService.checkout();
        verify(cartRepository).findByUser(user);
    }

    @Test
    void checkoutShouldCreateBookOrder() {
        User user = User.builder().id(1L).username("user").build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        Book book = Book.builder().id(1L).title("Clean Code").price(new BigDecimal("500")).stock(10).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.setItems(new ArrayList<>(List.of(cartItem)));
        SecurityContext securityContext =SecurityContextHolder.createEmptyContext();
        Authentication authentication =new UsernamePasswordAuthenticationToken("user", null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        cartService.checkout();
        verify(bookOrderRepository).save(any(BookOrder.class));
    }
    @Test
    void onCheckoutCreateOrderItemFromCartItemTest() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Clean Code").price(new BigDecimal("500"))
                .stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.setItems(new ArrayList<>(List.of(cartItem)));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        cartService.checkout();
        ArgumentCaptor<BookOrder> orderCaptor = ArgumentCaptor.forClass(BookOrder.class);
        verify(bookOrderRepository).save(orderCaptor.capture());
        BookOrder savedOrder = orderCaptor.getValue();
        assertEquals(1, savedOrder.getItems().size());
        OrderItem orderItem = savedOrder.getItems().get(0);
        assertEquals(book, orderItem.getBook());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(new BigDecimal("500"), orderItem.getPriceAtPurchase());
    }

    @Test
    void checkoutShouldCalculateOrderTotal() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Clean Code").price(new BigDecimal("500")).stock(10).build();        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.setItems(new ArrayList<>(List.of(cartItem)));
        SecurityContext securityContext =SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        cartService.checkout();
        ArgumentCaptor<BookOrder> orderCaptor = ArgumentCaptor.forClass(BookOrder.class);
        verify(bookOrderRepository).save(orderCaptor.capture());
        BookOrder savedOrder = orderCaptor.getValue();
        assertEquals(new BigDecimal("1000"),savedOrder.getTotalAmount());
    }

    @Test
    void checkoutShouldReduceBookStock() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Clean Code").price(new BigDecimal("500")).stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.setItems(new ArrayList<>(List.of(cartItem)));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        cartService.checkout();
        assertEquals(8, book.getStock());
        verify(bookRepository).save(book);
    }

    @Test
    void failCheckoutWhenStockIsInsufficientTest() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Clean Code").price(new BigDecimal("500")).stock(1).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.setItems(List.of(cartItem));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        assertThrows(InSufficientStockException.class,() -> cartService.checkout());
        assertEquals(1, book.getStock());
        verify(bookRepository, never()).save(book);
        verify(bookOrderRepository, never()).save(any(BookOrder.class));
    }

    @Test
    void checkoutShouldClearCartAfterSuccessfulCheckout() {
        User user = User.builder().id(1L).username("user").build();
        Book book = Book.builder().id(1L).title("Clean Code").price(new BigDecimal("500")).stock(10).build();
        Cart cart = Cart.builder().id(1L).user(user).build();
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).book(book).quantity(2).build();
        cart.setItems(new ArrayList<>(List.of(cartItem)));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", null);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        cartService.checkout();
        assertTrue(cart.getItems().isEmpty());
    }

}
