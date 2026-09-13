package com.kata.bookstore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddToCartRequest {
    private Long bookId;
    private int quantity;
}