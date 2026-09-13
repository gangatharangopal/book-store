package com.kata.bookstore.authentication;

import com.kata.bookstore.entity.BookOrder;
import com.kata.bookstore.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CartService cartService;

    @Test
    @WithMockUser(username = "user1", roles = "User")
    void shouldCheckoutCartSuccessfully() throws Exception {
        BookOrder order = BookOrder.builder().build();
        when(cartService.checkout()).thenReturn(order);
        mockMvc.perform(post("/api/cart/checkout").with(httpBasic("user1", "1234")))
                .andExpect(status().isCreated());
        verify(cartService).checkout();
    }

}