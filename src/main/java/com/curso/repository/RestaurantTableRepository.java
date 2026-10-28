package com.curso.repository;

import com.curso.model.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import com.curso.model.RestaurantTable; // Asegúrate de que el import coincida con tu entidad de mesas
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {

    boolean existsByNumber(Integer number);

    boolean existsByNumberAndIdNot(Integer number, Integer id);

    @Query("SELECT t FROM RestaurantTable t WHERE t.active = true AND" +
            " t.capacity >= :partySize AND NOT EXISTS (SELECT r.id FROM Reservation r WHERE r.mesa = t AND" +
            " r.status IN :statuses AND r.dateTime > :startTime AND r.dateTime < :endTime) ORDER BY t.capacity, t.number")
    List<RestaurantTable> findAvailable(@Param("partySize") Integer partySize, @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime, @Param("statuses") List<ReservationStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM RestaurantTable m WHERE m.id = :id")
    Optional<RestaurantTable> findByIdForUpdate(@Param("id") Integer id);
}
