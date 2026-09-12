package com.kata.bookstore.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;
    @Test
    void adminAuthenticationTest() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }
}
