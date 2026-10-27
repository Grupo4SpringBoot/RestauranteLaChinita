package com.curso.dto;

import com.curso.model.ReservationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReservationResponse {

    private Integer id;
    private Integer mesaId;
    private Integer numeroMesa;
    private LocalDateTime fechaHora;
    private Integer numPersonas;
    private ReservationStatus status;
    private Boolean confirmada;
    private Integer usuarioId;
}
