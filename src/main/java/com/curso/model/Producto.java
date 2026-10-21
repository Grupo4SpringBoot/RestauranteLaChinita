package com.curso.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "producto", schema = "public")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id; // Corresponde a 'id serial'

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre; // Corresponde a 'nombre character varying(100)'

    @Column(name = "precio", precision = 10, scale = 2, nullable = false)
    private BigDecimal precio; // Corresponde a 'precio numeric(10,2)' (Ideal para dinero)

    @Column(name = "tipo", length = 20)
    private String tipo; // Corresponde a 'tipo character varying(20)'

    @Column(name = "activo", nullable = false)
    private boolean activo; // Corresponde a 'activo boolean'
}
