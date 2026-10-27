package com.curso.dto;

import com.curso.model.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private ReservationStatus status;
}
