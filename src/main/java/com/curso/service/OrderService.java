package com.curso.service;

import com.curso.dto.CreateOrderItemRequest;
import com.curso.dto.CreateOrderRequest;
import com.curso.dto.OrderResponse;
import com.curso.exception.BusinessRuleException;
import com.curso.exception.ResourceNotFoundException;
import com.curso.mapper.OrderMapper;
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
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, ReservationRepository reservationRepository, ProductoRepository productoRepository
                        ,OrderMapper orderMapper   ) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.productoRepository = productoRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest){

        //validate basic data
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
        //Search for and validate the reservation
        Reservation reservation = reservationRepository
                .findById(createOrderRequest.getReservationId())
                .orElseThrow(()-> new ResourceNotFoundException("La reserva no existe"));

        if (reservation.getStatus()== ReservationStatus.CANCELADA ||
            reservation.getStatus() == ReservationStatus.COMPLETADA){
            throw new BusinessRuleException("No se pueden añadir pedidos a esta reserva");
        }

        //Create the order
        Order order = new Order();
        order.setReservation(reservation);
        order.setStatus(OrderStatus.PENDIENTE);

        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // 4. Create the details.
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

        //Save order and details
        order.setOrderDetails(orderDetails);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);

    }

    @Transactional
    public OrderResponse getOrderById(Integer id){
        if (id== null || id<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador debe ser mayor a 0");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No existe el pedido "+id));
        return orderMapper.toResponse(order);
    }


    @Transactional
    public List<OrderResponse> getOrdersByReservationId(Integer reservationId){

        if (reservationId == null || reservationId <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador de la reserva debe ser mayor a 0");
        }

        if (!reservationRepository.existsById(reservationId)){
            throw new ResourceNotFoundException("no existe la reserva "+ reservationId);
        }

        List <Order> orders =orderRepository.findByReservation_IdOrderByCreatedAtAscIdAsc(reservationId);

        List<OrderResponse> responses = new ArrayList<>();

        for(Order order : orders){
            responses.add(orderMapper.toResponse(order));
        }
        return responses;
    }
}
