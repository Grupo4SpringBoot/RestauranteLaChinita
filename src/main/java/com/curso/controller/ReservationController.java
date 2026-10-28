package com.curso.controller;

import com.curso.dto.ReservationRequest;
import com.curso.dto.ReservationResponse;
import com.curso.dto.ReservationStatusRequest;
import com.curso.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PreAuthorize("hasAuthority('reserva:crear')")
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
                                                      Authentication authentication) {
        ReservationResponse response = reservationService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('reserva:gestionar')")
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @PreAuthorize("hasAuthority('reserva:gestionar')or (hasAuthority('reserva:ver')and" +
            " @ownershipSecurity.ownsReservation(#id, authentication))")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Integer id) {
        return reservationService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize(" hasAuthority('reserva:gestionar')or (hasAuthority('reserva:crear')and " +
                 "@ownershipSecurity.ownsReservation(#id, authentication)and #request.status == T(com.curso.model.ReservationStatus).CANCELADA)")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReservationResponse> updateStatus(@PathVariable Integer id,
                                                             @Valid @RequestBody ReservationStatusRequest request) {
        return reservationService.updateStatus(id, request.getStatus())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('reserva:gestionar')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!reservationService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
