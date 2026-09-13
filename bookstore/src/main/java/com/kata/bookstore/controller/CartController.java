package com.kata.bookstore.controller;


import com.kata.bookstore.dto.AddToCartRequest;
import com.kata.bookstore.dto.CartBookResponse;
import com.kata.bookstore.dto.CartItemResponse;
import com.kata.bookstore.dto.CartResponse;
import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.repository.BookRepository;
import com.kata.bookstore.repository.UserRepository;
import com.kata.bookstore.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
    @PostMapping("/checkout")
    public ResponseEntity<BookOrder> checkout() {
        BookOrder checkout = cartService.checkout();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
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
}
