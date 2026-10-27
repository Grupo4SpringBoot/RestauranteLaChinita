package com.curso.mapper;

import com.curso.dto.ReservationRequest;
import com.curso.dto.ReservationResponse;
import com.curso.model.AppUser;
import com.curso.model.Reservation;
import com.curso.model.ReservationStatus;
import com.curso.model.RestaurantTable;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setMesaId(reservation.getMesa() != null ? reservation.getMesa().getId() : null);
        response.setNumeroMesa(reservation.getMesa() != null ? reservation.getMesa().getNumber() : null);
        response.setFechaHora(reservation.getDateTime());
        response.setNumPersonas(reservation.getNumGuests());
        response.setStatus(reservation.getStatus());
        response.setConfirmada(reservation.getConfirmed());
        response.setUsuarioId(reservation.getUser() != null ? reservation.getUser().getId() : null);
        return response;
    }

    public Reservation toEntity(ReservationRequest request, AppUser user, RestaurantTable mesa) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setMesa(mesa);
        reservation.setDateTime(request.getFechaHora());
        reservation.setNumGuests(request.getNumPersonas());
        reservation.setStatus(ReservationStatus.PENDIENTE);
        reservation.setConfirmed(false);
        return reservation;
    }
}
