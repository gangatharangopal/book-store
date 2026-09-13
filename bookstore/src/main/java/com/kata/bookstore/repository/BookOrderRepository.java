package com.kata.bookstore.repository;

import com.kata.bookstore.entity.BookOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookOrderRepository  extends JpaRepository<BookOrder, Long> {
}
