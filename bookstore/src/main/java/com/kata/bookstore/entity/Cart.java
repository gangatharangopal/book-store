package com.kata.bookstore.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL

    )
    private List<CartItem> items = new ArrayList<>();
}