package com.curso.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReservationRequest {

    @NotNull(message = "La mesa es obligatoria")
    @Positive(message = "La mesa debe ser un identificador valido")
    private Integer mesaId;

    @NotNull(message = "El numero de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos una persona")
    private Integer numPersonas;

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;
}
