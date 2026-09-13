package com.kata.bookstore.controller;


import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @PostMapping("/checkout")
    public ResponseEntity<BookOrder> checkout() {
        cartService.checkout();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
