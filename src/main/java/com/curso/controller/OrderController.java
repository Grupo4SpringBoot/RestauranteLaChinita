package com.curso.controller;

import com.curso.dto.CreateOrderRequest;
import com.curso.dto.OrderResponse;
import com.curso.dto.UpdateOrderRequest;
import com.curso.dto.UpdateOrderStatusRequest;
import com.curso.model.Reservation;
import com.curso.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {

        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/reserva/{reservationId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByReservationId(@PathVariable Integer reservationId){
        return ResponseEntity.ok(orderService.getOrdersByReservationId(reservationId));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Integer id, @Valid @RequestBody UpdateOrderStatusRequest updateOrderStatusRequest){
        return ResponseEntity.ok(orderService.updateOrderStatus(id, updateOrderStatusRequest));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest){
        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Integer id, @Valid @RequestBody UpdateOrderRequest updateOrderRequest){
        return ResponseEntity.ok(orderService.updateOrder(id, updateOrderRequest));
    }
}
