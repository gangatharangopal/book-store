package com.kata.bookstore.controller;


import com.kata.bookstore.dto.AddToCartRequest;
import com.kata.bookstore.dto.CartBookResponse;
import com.kata.bookstore.dto.CartItemResponse;
import com.kata.bookstore.dto.CartResponse;
import com.kata.bookstore.dto.UpdateCartQuantityRequest;
import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.repository.UserRepository;
import com.kata.bookstore.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;
    private final  BookRepository bookRepository;
    public CartController(CartService cartService, UserRepository userRepository, BookRepository bookRepository)
    {
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Checkout the current user's cart.
     */
    @PostMapping("/checkout")
    public ResponseEntity<BookOrder> checkout() {
        BookOrder checkout = cartService.checkout();
        return ResponseEntity.status(HttpStatus.CREATED).body(checkout);
    }

    /**
     * Add a book to the current user's cart.
     */
    @PostMapping
    public ResponseEntity<CartResponse> addBookToCart(@RequestBody AddToCartRequest request) {
        Cart cart = cartService.addBookToCart(request);
        CartResponse cartResponse = CartResponse.builder()
                .id(cart.getId())
                .items(cart.getItems().stream()
                        .map(item -> CartItemResponse.builder()
                                .id(item.getId())
                                .quantity(item.getQuantity())
                                .book(CartBookResponse.builder()
                                        .id(item.getBook().getId())
                                        .title(item.getBook().getTitle())
                                        .author(item.getBook().getAuthor())
                                        .price(item.getBook().getPrice())
                                        .build())
                                .build())
                        .toList())
                .build();
        return ResponseEntity.ok(cartResponse);
    }

    /**
     * Remove a book from the cart.
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Cart> removeBookFromCart(@PathVariable Long bookId) {
        Cart cart = cartService.removeBookFromCart(bookId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Get the current user's cart.
     */
    @GetMapping
    public ResponseEntity<Cart> getUserCart() {
        Cart cart = cartService.getUserCart();
        return ResponseEntity.ok(cart);
    }

    /**
     * Update the quantity of a book in the cart.
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<Cart> updateCartItemQuantity(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {
        Cart cart = cartService.updateCartItemQuantity(bookId,request.getQuantity());
        return ResponseEntity.ok(cart);
    }

}
