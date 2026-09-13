package com.kata.bookstore.service;


import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.CartItem;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.exception.QtyNotAvailableException;
import com.kata.bookstore.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    public Cart addBookToCart(User user, Book book, int qty) {
        if (book.getStock() <= 0) {
            throw new IllegalStateException("Book is out of stock: " + book.getTitle());
        }
        Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() ->cartRepository.save(Cart.builder().user(user).build()));
        Optional<CartItem> existingItem = cart.getItems().stream().filter(item -> item.getBook().getId().equals(book.getId())).findFirst();
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + qty;
            if (newQuantity > book.getStock()) {
                throw new QtyNotAvailableException("Requested quantity exceeds available stock");
            }
            item.setQuantity(item.getQuantity() + qty);
        } else {
            cart.getItems().add(CartItem.builder().cart(cart).book(book).quantity(qty).build());
        }
        return cartRepository.save(cart);
    }
}
