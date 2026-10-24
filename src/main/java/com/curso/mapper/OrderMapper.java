package com.curso.mapper;

import com.curso.dto.OrderItemResponse;
import com.curso.dto.OrderResponse;
import com.curso.model.Order;
import com.curso.model.OrderDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order){
        List<OrderItemResponse> items = new ArrayList<>();

        for (OrderDetail detail : order.getOrderDetails()){
            OrderItemResponse item = new OrderItemResponse();

            item.setId(detail.getId());
            item.setProductId(detail.getProduct().getId());
            item.setProductName(detail.getProduct().getNombre());
            item.setQuantity(detail.getQuantity());
            item.setUnitPrice(detail.getUnitPrice());
            item.setSubtotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));

            items.add(item);
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setReservationId((order.getReservation().getId()));
        orderResponse.setStatus(order.getStatus());
        orderResponse.setTotalPrice(order.getTotalPrice());
        orderResponse.setCreatedAt(order.getCreatedAt());
        orderResponse.setItems(items);

        return orderResponse;
    }
}
