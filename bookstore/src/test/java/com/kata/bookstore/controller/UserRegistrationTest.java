package com.kata.bookstore.controller;

import com.kata.bookstore.dto.RegistrationRequest;
import com.kata.bookstore.entity.Role;
import com.kata.bookstore.entity.User;
import com.kata.bookstore.exception.DuplicateUsernameException;
import com.kata.bookstore.service.CustomerUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserRegistrationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    CustomerUserService customerUserService;
    @Test
    void userRegistrationTest() throws Exception {
        String requestBody = """
            {   "username": "name1","password": "1234" }
            """;
        mockMvc.perform(post("/api/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
       .andExpect(status().isCreated());
    }
    @Test
    void duplicateUserRegistrationTest() throws Exception {
        String requestBody = """
            {   "username": "namex","password": "1234" }
            """;
        doThrow(new DuplicateUsernameException("User already exists")).when(customerUserService)
                .registerUser(any(RegistrationRequest.class));
        mockMvc.perform(post("/api/users/registration").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isConflict());
        verify(customerUserService).registerUser(any(RegistrationRequest.class));

    }
    @Test
    void userNameNotEmptyTest() throws Exception {
        String requestBody = """
            {   "username": "","password": "1234" }
            """;
        mockMvc.perform(post("/api/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());
    }
    @Test
    void userPasswordNotEmptyTest() throws Exception {
        String requestBody = """
            {   "username": "name1","password": "" }
            """;
        mockMvc.perform(post("/api/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin",roles = "Admin")
    void shouldGetAllUsers() throws Exception {
        User user1 = User.builder().id(1L).username("admin").role(Role.Admin).build();
        User user2 = User.builder().id(2L).username("user1").role(Role.User).build();
        when(customerUserService.getAllUsers()).thenReturn(List.of(user1, user2));
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("Admin"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("user1"))
                .andExpect(jsonPath("$[1].role").value("User"));
        verify(customerUserService).getAllUsers();

    }
}
