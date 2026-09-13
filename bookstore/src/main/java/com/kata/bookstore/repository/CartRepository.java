package com.kata.bookstore.repository;


import com.kata.bookstore.entity.Cart;
import com.kata.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}