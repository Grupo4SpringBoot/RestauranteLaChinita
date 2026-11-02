package com.curso.service;

import com.curso.dto.CreateOrderItemRequest;
import com.curso.dto.CreateOrderRequest;
import com.curso.dto.OrderResponse;
import com.curso.dto.UpdateOrderStatusRequest;
import com.curso.dto.UpdateOrderRequest;
import com.curso.exception.BusinessRuleException;
import com.curso.exception.ResourceNotFoundException;
import com.curso.mapper.OrderMapper;
import com.curso.model.Order;
import com.curso.model.OrderDetail;
import com.curso.model.OrderStatus;
import com.curso.model.Producto;
import com.curso.model.Reservation;
import com.curso.model.ReservationStatus;
import com.curso.repository.OrderRepository;
import com.curso.repository.ProductoRepository;
import com.curso.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndCalculateTotal() {
        Reservation reservation = createReservation(ReservationStatus.CONFIRMADA);

        Producto firstProduct = createProduct("Arroz", "12.50", true);
        Producto secondProduct = createProduct("Agua", "2.00", true);

        CreateOrderRequest request = createRequest(createItem(10, 2), createItem(20, 3));

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(productoRepository.findById(10)).thenReturn(Optional.of(firstProduct));
        when(productoRepository.findById(20)).thenReturn(Optional.of(secondProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse expectedResponse = new OrderResponse();

        when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

        OrderResponse result = orderService.createOrder(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        Order savedOrder = captor.getValue();

        assertSame(expectedResponse, result);
        assertSame(reservation, savedOrder.getReservation());
        assertEquals(OrderStatus.PENDIENTE, savedOrder.getStatus());
        assertEquals(2, savedOrder.getOrderDetails().size());

        // 12.50 × 2 + 2.00 × 3 = 31.00
        assertEquals(0, new BigDecimal("31.00").compareTo(savedOrder.getTotalPrice()));
        var firstDetail = savedOrder.getOrderDetails().get(0);

        assertSame(firstProduct, firstDetail.getProduct());
        assertSame(savedOrder, firstDetail.getOrder());
        assertEquals(Integer.valueOf(2), firstDetail.getQuantity());

        assertEquals(0, new BigDecimal("12.50").compareTo(firstDetail.getUnitPrice()));

        // The order keeps the price recorded at creation.
        firstProduct.setPrecio(new BigDecimal("15.00"));

        assertEquals(0, new BigDecimal("12.50").compareTo(firstDetail.getUnitPrice()));
    }

    @Test
    void shouldRejectOrderWithoutItems() {
        CreateOrderRequest request = createRequest();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(
                reservationRepository,
                productoRepository,
                orderRepository,
                orderMapper
        );
    }

    @Test
    void shouldRejectOrderWhenReservationDoesNotExist() {
        when(reservationRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(createRequest(createItem(10, 2))));

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(productoRepository, orderMapper);
    }

    @Test
    void shouldRejectOrderForCancelledReservation() {
        Reservation reservation = createReservation(ReservationStatus.CANCELADA);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        assertThrows(BusinessRuleException.class, () -> orderService.createOrder(createRequest(createItem(10, 2))));

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(productoRepository, orderMapper);
    }

    @Test
    void shouldRejectZeroQuantity() {
        when(reservationRepository.findById(1)).thenReturn(Optional.of(createReservation(ReservationStatus.CONFIRMADA)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(createRequest(createItem(10, 0))));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(productoRepository, orderMapper);
    }

    @Test
    void shouldRejectDuplicateProducts() {
        when(reservationRepository.findById(1)).thenReturn(Optional.of(createReservation(ReservationStatus.CONFIRMADA)));

        when(productoRepository.findById(10)).thenReturn(Optional.of(createProduct("Arroz", "12.50", true)));

        CreateOrderRequest request = createRequest(createItem(10, 1), createItem(10, 2));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(productoRepository, times(1)).findById(10);
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldRejectInactiveProduct() {
        when(reservationRepository.findById(1)).thenReturn(Optional.of(createReservation(ReservationStatus.CONFIRMADA)));

        when(productoRepository.findById(10)).thenReturn(Optional.of(createProduct("Arroz", "12.50", false)));

        assertThrows(BusinessRuleException.class, () -> orderService.createOrder(createRequest(createItem(10, 2))));

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldRejectDirectTransitionFromPendingToPaid() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDIENTE);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = createStatusRequest(OrderStatus.PAGADO);

        assertThrows(BusinessRuleException.class, () -> orderService.updateOrderStatus(1, request));

        assertEquals(OrderStatus.PENDIENTE, order.getStatus());

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldAllowTransitionFromPendingToServed() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDIENTE);
        order.setReservation(createReservation(ReservationStatus.CONFIRMADA));

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse expectedResponse = new OrderResponse();

        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        OrderResponse result = orderService.updateOrderStatus(1, createStatusRequest(OrderStatus.SERVIDO));

        assertSame(expectedResponse, result);
        assertEquals(OrderStatus.SERVIDO, order.getStatus());

        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectServingOrderForCancelledReservation() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDIENTE);
        order.setReservation(createReservation(ReservationStatus.CANCELADA));

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(BusinessRuleException.class, () -> orderService.updateOrderStatus(1, createStatusRequest(OrderStatus.SERVIDO)));

        assertEquals(OrderStatus.PENDIENTE, order.getStatus());

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldUpdatePendingOrder() {
        Order order = createOrderWithDetail("12.50", "12.50");
        OrderDetail removedDetail = new OrderDetail();
        Producto removedProduct = createProduct("Agua", "2.00", true);
        ReflectionTestUtils.setField(removedProduct, "id", 20);
        removedDetail.setProduct(removedProduct);
        removedDetail.setQuantity(1);
        removedDetail.setUnitPrice(new BigDecimal("2.00"));
        order.getOrderDetails().add(removedDetail);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        OrderResponse expected = new OrderResponse();
        when(orderMapper.toResponse(order)).thenReturn(expected);

        assertSame(expected, orderService.updateOrder(1, createUpdateRequest(3)));

        assertEquals(1, order.getOrderDetails().size());
        assertEquals(Integer.valueOf(3), order.getOrderDetails().get(0).getQuantity());
        assertEquals(0, new BigDecimal("37.50").compareTo(order.getTotalPrice()));
        verify(orderRepository).save(order);
        verifyNoInteractions(productoRepository);
    }

    @Test
    void shouldPreserveHistoricalPriceWhenUpdatingOrder() {
        Order order = createOrderWithDetail("12.50", "20.00");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        OrderResponse expected = new OrderResponse();
        when(orderMapper.toResponse(order)).thenReturn(expected);

        assertSame(expected, orderService.updateOrder(1, createUpdateRequest(3)));

        assertEquals(0, new BigDecimal("12.50")
                .compareTo(order.getOrderDetails().get(0).getUnitPrice()));
        assertEquals(0, new BigDecimal("37.50").compareTo(order.getTotalPrice()));
        verify(orderRepository).save(order);
        verifyNoInteractions(productoRepository);
    }

    @Test
    void shouldRejectUpdatingPaidOrder() {
        Order order = createOrderWithDetail("12.50", "12.50");
        order.setStatus(OrderStatus.PAGADO);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(BusinessRuleException.class,
                () -> orderService.updateOrder(1, createUpdateRequest(3)));

        assertEquals(Integer.valueOf(1), order.getOrderDetails().get(0).getQuantity());
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(productoRepository, orderMapper);
    }

    @Test
    void shouldAllowTransitionFromServedToPaid() {
        Order order = new Order();
        order.setStatus(OrderStatus.SERVIDO);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        OrderResponse expected = new OrderResponse();
        when(orderMapper.toResponse(order)).thenReturn(expected);

        assertSame(expected, orderService.updateOrderStatus(
                1, createStatusRequest(OrderStatus.PAGADO)));

        assertEquals(OrderStatus.PAGADO, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldCancelOnlyPendingOrders() {
        Order pending = new Order();
        Order served = new Order();
        served.setStatus(OrderStatus.SERVIDO);
        Order paid = new Order();
        paid.setStatus(OrderStatus.PAGADO);
        Order cancelled = new Order();
        cancelled.setStatus(OrderStatus.CANCELADO);
        when(reservationRepository.existsById(1)).thenReturn(true);
        when(orderRepository.findByReservation_IdOrderByCreatedAtAscIdAsc(1))
                .thenReturn(List.of(pending, served, paid, cancelled));

        orderService.cancelPedingOrdersByReservationId(1);

        assertEquals(OrderStatus.CANCELADO, pending.getStatus());
        assertEquals(OrderStatus.SERVIDO, served.getStatus());
        assertEquals(OrderStatus.PAGADO, paid.getStatus());
        assertEquals(OrderStatus.CANCELADO, cancelled.getStatus());
    }

    private UpdateOrderRequest createUpdateRequest(int quantity) {
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setItems(List.of(createItem(10, quantity)));
        return request;
    }

    private Order createOrderWithDetail(String historicalPrice, String currentPrice) {
        Producto product = createProduct("Arroz", currentPrice, true);
        // IDs are normally assigned by JPA and have no public setter.
        ReflectionTestUtils.setField(product, "id", 10);
        Order order = new Order();
        order.setReservation(createReservation(ReservationStatus.CONFIRMADA));
        order.setTotalPrice(new BigDecimal(historicalPrice));
        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setOrder(order);
        detail.setQuantity(1);
        detail.setUnitPrice(new BigDecimal(historicalPrice));
        order.setOrderDetails(new ArrayList<>(List.of(detail)));
        return order;
    }

    private Reservation createReservation(ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setStatus(status);
        return reservation;
    }

    private Producto createProduct(String name, String price, boolean active) {
        Producto product = new Producto();
        product.setNombre(name);
        product.setPrecio(new BigDecimal(price));
        product.setActivo(active);
        return product;
    }

    private CreateOrderItemRequest createItem(Integer productId, Integer quantity) {
        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private CreateOrderRequest createRequest(CreateOrderItemRequest... items) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReservationId(1);
        request.setItems(List.of(items));
        return request;
    }

    private UpdateOrderStatusRequest createStatusRequest(OrderStatus status) {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();

        request.setStatus(status);
        return request;
    }
}
