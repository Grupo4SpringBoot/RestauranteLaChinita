package com.curso.service;

import com.curso.dto.*;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Set<Integer> productIds = new HashSet<>();
        // 4. Create the details.
        for (CreateOrderItemRequest item : createOrderRequest.getItems()){

            if (item == null || item.getProductId() == null ||
                item.getQuantity() == null || item.getProductId()<=0 ||
                item.getQuantity()<=0){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada producto debe tener identificador y cantidad positivos");
            }

            if (!productIds.add(item.getProductId()))
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se repiten los productos, cambie la cantidad");
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
        if (total.compareTo(new BigDecimal("99999999.99"))>0){
            throw new BusinessRuleException("El total supera el importe permitido");
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

    @Transactional
    public OrderResponse updateOrder(Integer id, UpdateOrderRequest updateOrderRequest){

        if (id== null || id<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador debe ser mayor a 0");
        }

        if(updateOrderRequest ==null || updateOrderRequest.getItems()== null || updateOrderRequest.getItems().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe contener al menos un producto");
        }

        Order order = orderRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("no existe el pedido "+id));

        if (order.getStatus() != OrderStatus.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden modificar pedidos pendientes");
        }

        ReservationStatus reservationStatus = order.getReservation().getStatus();

        if (reservationStatus == ReservationStatus.CANCELADA || reservationStatus == ReservationStatus.COMPLETADA) {
            throw new BusinessRuleException("No se puede modificar un pedido de una reserva " + "cancelada o completada");
        }

        Set<Integer> productIds = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemRequest item : updateOrderRequest.getItems()){
            if(item== null || item.getProductId()==null || item.getQuantity()== null || item.getProductId()<=0 || item.getQuantity()<=0){
                throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada producto debe tener su identificador positivo");
            }

            if (!productIds.add(item.getProductId())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede repetir el producto, por favor cambie la cantidad del producto que quiere repetir");
            }

            OrderDetail orderDetail= null;
            for (OrderDetail existing: order.getOrderDetails()){
                if (existing.getProduct().getId().equals(item.getProductId())){
                    if (orderDetail!= null){
                        throw new BusinessRuleException("El pedido contiene detalles repetidos del producto :"+item.getProductId());
                    }
                    orderDetail = existing;
                }
            }

            if (orderDetail != null) {
                if (!orderDetail.getProduct().isActivo() && item.getQuantity() > orderDetail.getQuantity()) {

                    throw new BusinessRuleException("No se puede aumentar la cantidad de un producto inactivo");
                }

                orderDetail.setQuantity(item.getQuantity());

            } else {
                Producto product = productoRepository.findById(item.getProductId()).orElseThrow(() -> new
                        ResourceNotFoundException("No existe el producto " + item.getProductId()));

                if (!product.isActivo()) {
                    throw new BusinessRuleException("El producto " + product.getNombre() + " no está disponible");
                }

                orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setProduct(product);
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setUnitPrice(product.getPrecio());

                order.getOrderDetails().add(orderDetail);
            }

            BigDecimal subtotal = orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity()));

            total = total.add(subtotal);
        }

        if (total.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new BusinessRuleException("El total supera el importe permitido");
        }

        // Retirar únicamente los productos que no aparecen en la lista enviada.
        order.getOrderDetails().removeIf(detail -> !productIds.contains(detail.getProduct().getId()));

        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Integer id, UpdateOrderStatusRequest updateOrderStatusRequest){
        if (id == null || id<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador debe ser mayor a 0");
        }

        if (updateOrderStatusRequest == null || updateOrderStatusRequest.getStatus() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        Order order = orderRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("no existe el pedido: "+id));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = updateOrderStatusRequest.getStatus();

        if (currentStatus== newStatus){
            return orderMapper.toResponse(order);
        }
        boolean allowed = switch(currentStatus){
            case PENDIENTE ->newStatus == OrderStatus.SERVIDO || newStatus == OrderStatus.CANCELADO;
            case SERVIDO -> newStatus == OrderStatus.PAGADO;
            case PAGADO, CANCELADO -> false;
        };
        if (!allowed) {
            throw new BusinessRuleException("No se permite cambiar de: "+currentStatus+ " a "+newStatus);
        }

        if (newStatus== OrderStatus.SERVIDO){
            ReservationStatus reservationStatus = order.getReservation().getStatus();

            if (reservationStatus == ReservationStatus.CANCELADA || reservationStatus == ReservationStatus.COMPLETADA){
                throw new BusinessRuleException("No se puede servir un pedido de una reserva: CANCELADA o COMPLETADA");
            }
        }
        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public void cancelPedingOrdersByReservationId(Integer reservationId){
        if (reservationId== null || reservationId<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador de la reserva debe ser mayor a 0");
        }

        if (!reservationRepository.existsById(reservationId)){
            throw new ResourceNotFoundException("No existe la reservacion "+ reservationId);
        }

        List<Order> orders = orderRepository.findByReservation_IdOrderByCreatedAtAscIdAsc(reservationId);

        for (Order order : orders){
            if (order.getStatus() == OrderStatus.PENDIENTE){
                order.setStatus(OrderStatus.CANCELADO);
            }
        }
    }
}

