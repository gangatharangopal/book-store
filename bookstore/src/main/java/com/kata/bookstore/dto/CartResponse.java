package com.kata.bookstore.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {

    private Long id;
    private List<CartItemResponse> items;
}