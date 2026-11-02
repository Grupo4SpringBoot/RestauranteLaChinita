package com.curso.service;

import com.curso.model.Reservation;
import com.curso.model.ReservationStatus;
import com.curso.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationExpiryService {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryService.class);

    /** Minutos que una reserva PENDIENTE puede esperar sin confirmarse antes de cancelarse. */
    private static final int MINUTOS_EXPIRACION = 60;

    /** Cada cuanto se revisa si hay reservas caducadas (milisegundos). */
    private static final long INTERVALO_REVISION_MS = 180_000;

    /** Espera inicial tras arrancar la app (milisegundos): deja que la app se asiente antes del primer barrido. */
    private static final long RETARDO_INICIAL_MS = 60_000;

    private final ReservationRepository reservationRepository;
    private final OrderService orderService;

    @Scheduled(initialDelay = RETARDO_INICIAL_MS, fixedDelay = INTERVALO_REVISION_MS)
    @Transactional
    public void cancelarPendientesExpiradas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_EXPIRACION);

        List<Reservation> caducadas = reservationRepository
                .findByStatusAndCreatedAtBefore(ReservationStatus.PENDIENTE, limite);

        for (Reservation reservation : caducadas) {
            reservation.setStatus(ReservationStatus.CANCELADA);
            reservation.setConfirmed(false);
            reservationRepository.save(reservation);

            orderService.cancelPedingOrdersByReservationId(reservation.getId());

            log.info("Reserva {} cancelada por no confirmarse en {} minutos (mesa {}, fecha {})",
                    reservation.getId(), MINUTOS_EXPIRACION,
                    reservation.getMesa() != null ? reservation.getMesa().getId() : null,
                    reservation.getDateTime());
        }
    }
}
