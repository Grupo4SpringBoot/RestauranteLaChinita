package com.curso.mapper;

import com.curso.dto.RestaurantTableRequest;
import com.curso.dto.RestaurantTableResponse;
import com.curso.model.RestaurantTable;
import org.springframework.stereotype.Component;

@Component
public class RestaurantTableMapper {

    public RestaurantTableResponse toResponse(RestaurantTable table) {
        RestaurantTableResponse response = new RestaurantTableResponse();
        response.setId(table.getId());
        response.setNumero(table.getNumber());
        response.setCapacidad(table.getCapacity());
        response.setUbicacion(table.getLocation());
        response.setActiva(table.getActive());
        return response;
    }

    public RestaurantTable toEntity(RestaurantTableRequest request) {
        RestaurantTable table = new RestaurantTable();
        table.setNumber(request.getNumero());
        table.setCapacity(request.getCapacidad());
        table.setLocation(request.getUbicacion());
        if (request.getActiva() != null) {
            table.setActive(request.getActiva());
        }
        return table;
    }
}
