package com.kata.bookstore.Service;


import com.kata.bookstore.dto.RegistrationRequest;
import com.kata.bookstore.entity.Role;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.exception.DuplicateUsernameException;
import com.kata.bookstore.repository.UserRepository;
import com.kata.bookstore.service.CustomerUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerUserServiceTest {
    @InjectMocks
    private CustomerUserService customerUserService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void registerNewUserTest() {
        RegistrationRequest request = RegistrationRequest.builder().username("user1").password("1234")
                .build();
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encoded-password");
        User savedUser = User.builder().id(1L).username("user1")
                .password("encoded-password").role(Role.User)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        User result = customerUserService.registerUser(request);
        assertNotNull(result);
        assertEquals(Optional.of(1L).get(), result.getId());
        assertEquals("user1", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertEquals(Role.User, result.getRole());
        verify(userRepository).existsByUsername("user1");
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenUsernameAlreadyExists() {
        RegistrationRequest request = RegistrationRequest.builder().username("user1")
                .password("1234").build();
        when(userRepository.existsByUsername("user1")).thenReturn(true);
        DuplicateUsernameException exception = assertThrows(
                DuplicateUsernameException.class,
                () -> customerUserService.registerUser(request)
        );
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("user1");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
