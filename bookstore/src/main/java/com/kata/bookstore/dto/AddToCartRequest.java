package com.kata.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddToCartRequest {
    @NotNull
    private Long bookId;

    @Min(1)
    private int quantity;
}
