package com.kata.bookstore.service;


import com.kata.bookstore.entity.Book;
import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.User;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    public Cart addBookToCart(User user, Book book, int qty) {
        Cart cart=new Cart();
        return cart;
    }
}
