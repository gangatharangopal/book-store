package com.kata.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class RegistrationRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
