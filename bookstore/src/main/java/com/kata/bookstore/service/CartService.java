package com.kata.bookstore.service;


import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.CartItem;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    public Cart addBookToCart(User user, Book book, int qty) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() ->cartRepository.save(Cart.builder().user(user).build())
                );
        CartItem cartItem = CartItem.builder().cart(cart).book(book).quantity(qty).build();
        cart.getItems().add(cartItem);
        return cartRepository.save(cart);
    }
}
