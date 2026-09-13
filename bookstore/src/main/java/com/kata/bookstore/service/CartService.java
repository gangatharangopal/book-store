package com.kata.bookstore.service;


import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.CartItem;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.exception.QtyNotAvailableException;
import com.kata.bookstore.exception.ResourceNotFoundException;
import com.kata.bookstore.repository.CartRepository;
import com.kata.bookstore.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    public CartService(CartRepository cartRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
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

    public Cart getUserCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() ->
                Cart.builder().user(user).build());
    }

    public Cart updateCartItemQuantity(User user, Long bookId, int qty) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->new ResourceNotFoundException("Cart not found for user: " + user.getUsername()));
        CartItem cartItem = cart.getItems().stream().filter(item ->
                        item.getBook().getId().equals(bookId)).findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found in cart: " + bookId));
        if (qty <= 0) {
            throw new IllegalStateException("Quantity must be greater than zero");
        }
        if (qty > cartItem.getBook().getStock()) {
            throw new IllegalStateException("Requested quantity exceeds available stock");
        }
        cartItem.setQuantity(qty);
        return cartRepository.save(cart);
    }

    public Cart removeBookFromCart(User user, Long bookId) {
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getUsername()));
        CartItem cartItem = cart.getItems()
                .stream().filter(item ->item.getBook().getId().equals(bookId))
                .findFirst().orElseThrow(() ->
                        new ResourceNotFoundException("Book not found in cart: " + bookId));
        cart.getItems().remove(cartItem);
        return cartRepository.save(cart);
    }

    public BookOrder checkout() {
        BookOrder bookOrder = new BookOrder();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        return bookOrder;
    }
}
