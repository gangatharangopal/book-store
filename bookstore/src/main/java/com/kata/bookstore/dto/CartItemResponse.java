package com.kata.bookstore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {

    private Long id;
    private CartBookResponse book;
    private Integer quantity;
}