package com.curso.security;

import com.curso.repository.OrderRepository;
import com.curso.repository.ReservationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ownershipSecurity")
public class OwnershipSecurity {

    private final ReservationRepository reservationRepository;
    private final OrderRepository orderRepository;

    public OwnershipSecurity(ReservationRepository reservationRepository, OrderRepository orderRepository) {
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
    }

    public boolean ownsReservation(Integer id, Authentication authentication){

        return authentication!= null && authentication.isAuthenticated() && id!=null &&
                reservationRepository.existsByIdAndUser_Email(id, authentication.getName());
    }

    public boolean ownsOrder(Integer id, Authentication authentication){
        return authentication != null && authentication.isAuthenticated() && id != null
                && orderRepository.existsByIdAndReservation_User_Email(id, authentication.getName());
    }
}
