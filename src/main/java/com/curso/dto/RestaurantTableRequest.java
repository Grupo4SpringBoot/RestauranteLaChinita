package com.curso.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantTableRequest {

    @NotNull(message = "El numero de mesa es obligatorio")
    @Positive(message = "El numero de mesa debe ser positivo")
    private Integer numero;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser positiva")
    private Integer capacidad;

    @Size(max = 50, message = "La ubicacion no puede superar los 50 caracteres")
    private String ubicacion;

    private Boolean activa;
}
