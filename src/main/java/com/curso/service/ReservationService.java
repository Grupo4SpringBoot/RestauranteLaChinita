package com.curso.service;

import com.curso.dto.ReservationRequest;
import com.curso.dto.ReservationResponse;
import com.curso.exception.BusinessRuleException;
import com.curso.exception.ResourceNotFoundException;
import com.curso.mapper.ReservationMapper;
import com.curso.model.AppUser;
import com.curso.model.Reservation;
import com.curso.model.ReservationStatus;
import com.curso.model.RestaurantTable;
import com.curso.repository.AppUserRepository;
import com.curso.repository.ReservationRepository;
import com.curso.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final AppUserRepository userRepository;
    private final ReservationMapper reservationMapper;

    @Transactional
    public ReservationResponse create(ReservationRequest request, String email) {
        AppUser user = resolveUsuario(email);

        LocalDateTime  reservationTime = request.getFechaHora();
        if (reservationTime==null || !reservationTime.isAfter(LocalDateTime.now())){
            throw  new ResponseStatusException(BAD_REQUEST, "La fecha y hora debes ser futuras");
        }

        RestaurantTable mesa = resolveMesa(request.getMesaId());

        if (request.getNumPersonas()== null || request.getNumPersonas() <=0){
            throw new ResponseStatusException(BAD_REQUEST, "El numero de personas debe ser mayor que cero");
        }

        if (request.getNumPersonas() > mesa.getCapacity()) {
            throw new BusinessRuleException("La mesa " + mesa.getNumber()
                    + " tiene una capacidad de " + mesa.getCapacity() + " personas");
        }

        LocalDateTime overlapLowerBound = reservationTime.minusHours(2);
        LocalDateTime reservationEnd = reservationTime.plusHours(2);

        boolean occupied =reservationRepository.existsByMesa_IdAndStatusInAndDateTimeGreaterThanAndDateTimeLessThan(mesa.getId(),
                List.of(ReservationStatus.PENDIENTE, ReservationStatus.CONFIRMADA), overlapLowerBound, reservationEnd);

        if (occupied){
            throw new BusinessRuleException("La mesa ya tiene una reserva que coincide con ese horario");
        }

        Reservation reservation = reservationMapper.toEntity(request, user, mesa);
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public Optional<ReservationResponse> findById(Integer id) {
        return reservationRepository.findById(id).map(reservationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream().map(reservationMapper::toResponse).toList();
    }

    @Transactional
    public Optional<ReservationResponse> updateStatus(Integer id, ReservationStatus status) {
        Optional<Reservation> found = reservationRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        Reservation reservation = found.get();
        reservation.setStatus(status);
        reservation.setConfirmed(status == ReservationStatus.CONFIRMADA);

        return Optional.of(reservationMapper.toResponse(reservationRepository.save(reservation)));
    }

    @Transactional
    public boolean delete(Integer id) {
        Optional<Reservation> found = reservationRepository.findById(id);
        if (found.isEmpty()) {
            return false;
        }

        Reservation reservation = found.get();
        if (!reservation.getOrders().isEmpty()) {
            throw new BusinessRuleException("La reserva " + id + " tiene pedidos asociados y no se puede eliminar");
        }

        reservationRepository.delete(reservation);
        return true;
    }

    private AppUser resolveUsuario(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "El usuario autenticado es obligatorio");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + email));
    }

    private RestaurantTable resolveMesa(Integer mesaId) {
        RestaurantTable mesa = tableRepository.findByIdForUpdate(mesaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la mesa " + mesaId));

        if (Boolean.FALSE.equals(mesa.getActive())) {
            throw new BusinessRuleException("La mesa " + mesa.getNumber() + " no esta activa");
        }

        return mesa;
    }
}