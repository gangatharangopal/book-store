package com.kata.bookstore.controller;

import com.kata.bookstore.dto.RegistrationRequest;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.service.CustomerUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final CustomerUserService customerUserService;
    public UserController(CustomerUserService customerUserService) {
        this.customerUserService = customerUserService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(customerUserService.getAllUsers());
    }

    /**
     * Register a new user.
     */
    @PostMapping("/registration")
    @SecurityRequirements
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest request) {
        customerUserService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
