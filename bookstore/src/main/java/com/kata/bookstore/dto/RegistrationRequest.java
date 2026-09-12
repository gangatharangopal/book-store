package com.kata.bookstore.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
@Data
public class RegistrationRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
