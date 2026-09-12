package com.kata.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     *  H2 Console is used during development/testing to
     *  inspect the in-memory database and verify tables/data. As H2 uses I-frame for
     *  login spring security blocks it.
     *  so allow frames from the same application origin for the H2 Console.
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //http://localhost:8080/h2-console
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/h2-console/**").permitAll().anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}