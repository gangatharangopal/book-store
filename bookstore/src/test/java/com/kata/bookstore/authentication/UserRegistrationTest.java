package com.kata.bookstore.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class UserRegistrationTest {
    @Autowired
    private MockMvc mockMvc;
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
            {   "username": "name1","password": "1234" }
            """;
        mockMvc.perform(post("/api/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/users/registration").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict());
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
}
