package com.kata.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     *  H2 Console used during development/testing to
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
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth ->
                            auth
                                .requestMatchers(HttpMethod.POST,"/api/users/registration").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("Admin")
                                .requestMatchers("/h2-console/**","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**").permitAll()
                                .requestMatchers(HttpMethod.POST,"/api/books").hasRole("Admin")
                                .requestMatchers(HttpMethod.PUT,"/api/books").hasRole("Admin")
                                .requestMatchers(HttpMethod.POST,"/api/cart").hasAnyRole("Admin","User")
                                .requestMatchers(HttpMethod.PUT,"/api/cart").hasAnyRole("Admin","User")
                                .requestMatchers(HttpMethod.GET,"/api/cart").hasAnyRole("Admin","User")
                            .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}