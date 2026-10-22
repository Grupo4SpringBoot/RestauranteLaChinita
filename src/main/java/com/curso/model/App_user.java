package com.curso.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List; // Importante para poder usar List<>

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class App_user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private int id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // Relación colocada CORRECTAMENTE dentro de la clase
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Evita bucles infinitos al transformar a JSON
    private List<Order> pedidos;
}

