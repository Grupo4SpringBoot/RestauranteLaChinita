package com.curso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.curso.model.Reservation; // Revisa si tu entidad está en este paquete

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
}
