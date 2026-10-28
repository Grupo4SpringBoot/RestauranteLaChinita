package com.curso.repository;

import com.curso.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Integer> {
    List<Order> findByReservation_IdOrderByCreatedAtAscIdAsc(Integer reservationId);

    boolean existsByIdAndReservation_User_Email(Integer id, String reservationUserEmail);
}
