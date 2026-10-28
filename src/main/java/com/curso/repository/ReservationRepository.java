package com.curso.repository;

import com.curso.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.curso.model.Reservation; // Revisa si tu entidad está en este paquete

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {



    boolean existsByIdAndUser_Email(Integer id, String userEmail);
    boolean existsByMesa_IdAndStatusInAndDateTimeGreaterThanAndDateTimeLessThan(Integer mesaId,
                                                                                List<ReservationStatus> statuses,
                                                                                LocalDateTime limiteAnterior, LocalDateTime fin);
}
