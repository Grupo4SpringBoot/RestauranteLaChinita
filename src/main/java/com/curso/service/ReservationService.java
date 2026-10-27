package com.curso.service;

import com.curso.dto.OrderItemResponse;
import com.curso.dto.OrderResponse;
import com.curso.dto.ReservationRequest;
import com.curso.dto.ReservationResponse;
import com.curso.model.AppUser;
import com.curso.model.Order;
import com.curso.model.OrderDetail;
import com.curso.model.Reservation;
import com.curso.model.ReservationStatus;
import com.curso.model.RestaurantTable;
import com.curso.repository.AppUserRepository;
import com.curso.repository.OrderDetailRepository;
import com.curso.repository.ReservationRepository;
import com.curso.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final AppUserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    public ReservationResponse create(ReservationRequest request, AppUser usuario) {
        AppUser user = resolveUsuario(usuario);
        RestaurantTable mesa = resolveMesa(request.getMesaId());

        if (request.getNumPersonas() > mesa.getCapacity()) {
            throw new IllegalArgumentException("La mesa " + mesa.getNumber()
                    + " tiene una capacidad de " + mesa.getCapacity() + " personas");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setMesa(mesa);
        reservation.setDateTime(request.getFechaHora());
        reservation.setNumGuests(request.getNumPersonas());
        reservation.setStatus(ReservationStatus.PENDIENTE);
        reservation.setConfirmed(false);

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public Optional<ReservationResponse> findById(Integer id) {
        return reservationRepository.findById(id).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream().map(this::toResponse).toList();
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

        return Optional.of(toResponse(reservationRepository.save(reservation)));
    }

    @Transactional
    public boolean delete(Integer id) {
        Optional<Reservation> found = reservationRepository.findById(id);
        if (found.isEmpty()) {
            return false;
        }

        Reservation reservation = found.get();
        if (!reservation.getOrders().isEmpty()) {
            throw new IllegalStateException("La reserva " + id + " tiene pedidos asociados y no se puede eliminar");
        }

        reservationRepository.delete(reservation);
        return true;
    }

    private AppUser resolveUsuario(AppUser usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("El usuario autenticado es obligatorio");
        }
        return userRepository.findById(usuario.getId())
                .orElseThrow(() -> new NoSuchElementException("El usuario " + usuario.getId() + " no existe"));
    }

    private RestaurantTable resolveMesa(Integer mesaId) {
        RestaurantTable mesa = tableRepository.findById(mesaId)
                .orElseThrow(() -> new NoSuchElementException("La mesa " + mesaId + " no existe"));

        if (Boolean.FALSE.equals(mesa.getActive())) {
            throw new IllegalStateException("La mesa " + mesa.getNumber() + " no esta activa");
        }

        return mesa;
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setMesaId(reservation.getMesa() != null ? reservation.getMesa().getId() : null);
        response.setNumeroMesa(reservation.getMesa() != null ? reservation.getMesa().getNumber() : null);
        response.setFechaHora(reservation.getDateTime());
        response.setNumPersonas(reservation.getNumGuests());
        response.setStatus(reservation.getStatus());
        response.setConfirmada(reservation.getConfirmed());
        response.setUsuarioId(reservation.getUser() != null ? reservation.getUser().getId() : null);
        response.setPedidos(reservation.getOrders().stream().map(this::toOrderResponse).toList());
        return response;
    }

    private OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setReservationId(order.getReservation() != null ? order.getReservation().getId() : null);
        response.setStatus(order.getStatus());
        response.setTotalPrice(order.getTotalPrice());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(orderDetailRepository.findByOrderId(order.getId()).stream().map(this::toOrderItemResponse).toList());
        return response;
    }

    private OrderItemResponse toOrderItemResponse(OrderDetail detail) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(detail.getId());
        response.setProductId(detail.getProduct() != null ? detail.getProduct().getId() : null);
        response.setProductName(detail.getProduct() != null ? detail.getProduct().getNombre() : null);
        response.setQuantity(detail.getQuantity());
        response.setUnitPrice(detail.getUnitPrice());
        response.setSubtotal(detail.getUnitPrice() != null
                ? detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()))
                : BigDecimal.ZERO);
        return response;
    }
}
