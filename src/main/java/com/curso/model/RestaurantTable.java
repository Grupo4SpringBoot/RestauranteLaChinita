package com.curso.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@jakarta.persistence.Table(name = "mesa")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "numero", nullable = false, unique = true)
    private Integer number;

    @Column(name = "capacidad", nullable = false)
    private Integer capacity;

    @Column(name = "ubicacion", length = 50)
    private String location;

    @Column(name = "activa")
    private Boolean active = true;


}