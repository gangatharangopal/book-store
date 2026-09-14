package com.kata.bookstore.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartBookResponse {

    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
}