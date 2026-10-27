package com.curso.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantTableResponse {

    private Integer id;
    private Integer numero;
    private Integer capacidad;
    private String ubicacion;
    private Boolean activa;
}
