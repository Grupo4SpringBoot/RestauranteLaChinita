package com.curso.service;

import com.curso.dto.CreateOrderItemRequest;
import com.curso.dto.CreateOrderRequest;
import com.curso.dto.OrderItemResponse;
import com.curso.dto.OrderResponse;
import com.curso.exception.BusinessRuleException;
import com.curso.exception.ResourceNotFoundException;
import com.curso.model.*;
import com.curso.repository.OrderRepository;
import com.curso.repository.ProductoRepository;
import com.curso.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final ProductoRepository productoRepository;

    public OrderService(OrderRepository orderRepository, ReservationRepository reservationRepository, ProductoRepository productoRepository) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest){

        //validar los datos basicos
        if (createOrderRequest ==null ||
                createOrderRequest.getReservationId()== null ||
                createOrderRequest.getReservationId()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes indicar una reserva valida");
        }

        if (createOrderRequest.getItems()== null ||
            createOrderRequest.getItems().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El pedido debe contener al menos un producto");
        }
        //Buscar y validar la reserva
        Reservation reservation = reservationRepository
                .findById(createOrderRequest.getReservationId())
                .orElseThrow(()-> new ResourceNotFoundException("La reserva no existe"));

        if (reservation.getStatus()== ReservationStatus.CANCELADA ||
            reservation.getStatus() == ReservationStatus.COMPLETADA){
            throw new BusinessRuleException("No se pueden añadir pedidos a esta reserva");
        }

        //Crear el pedido
        Order order = new Order();
        order.setReservation(reservation);
        order.setStatus(OrderStatus.PENDIENTE);

        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // 4. Crear los detalles.
        for (CreateOrderItemRequest item : createOrderRequest.getItems()){

            if (item == null || item.getProductId() == null ||
                item.getQuantity() == null || item.getProductId()<=0 ||
                item.getQuantity()<=0){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada producto debe tener identificador y cantidad positivos");
            }

            Producto product = productoRepository
                    .findById(item.getProductId())
                    .orElseThrow(()-> new ResourceNotFoundException("No existe el producto "+item.getProductId()));

            if (!product.isActivo()){
                throw new BusinessRuleException("El producto "+ product.getNombre() + " no esta disponible");
            }

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setQuantity(item.getQuantity());
            orderDetail.setUnitPrice(product.getPrecio());

            orderDetails.add(orderDetail);

            BigDecimal subTotal = orderDetail.getUnitPrice()
                    .multiply(BigDecimal.valueOf(orderDetail.getQuantity()));
            total= total.add(subTotal);
        }

        //Guardar pedido y detalles
        order.setOrderDetails(orderDetails);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);

        List<OrderItemResponse> responseItems = new ArrayList<>();


        for (OrderDetail detail : savedOrder.getOrderDetails()) {

            OrderItemResponse responseItem = new OrderItemResponse();
            responseItem.setId(detail.getId());
            responseItem.setProductId(detail.getProduct().getId());
            responseItem.setProductName(detail.getProduct().getNombre());
            responseItem.setQuantity(detail.getQuantity());
            responseItem.setUnitPrice(detail.getUnitPrice());
            responseItem.setSubtotal(
                    detail.getUnitPrice()
                            .multiply(BigDecimal.valueOf(detail.getQuantity()))
            );

            responseItems.add(responseItem);
        }

        // Construir y devolver la respuesta.
        OrderResponse response = new OrderResponse();
        response.setId(savedOrder.getId());
        response.setReservationId(reservation.getId());
        response.setStatus(savedOrder.getStatus());
        response.setTotalPrice(savedOrder.getTotalPrice());
        response.setCreatedAt(savedOrder.getCreatedAt());
        response.setItems(responseItems);

        return response;

    }
}
