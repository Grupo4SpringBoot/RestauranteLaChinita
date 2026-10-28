package com.curso.service;

import com.curso.dto.RestaurantTableRequest;
import com.curso.dto.RestaurantTableResponse;
import com.curso.exception.BusinessRuleException;
import com.curso.mapper.RestaurantTableMapper;
import com.curso.model.ReservationStatus;
import com.curso.model.RestaurantTable;
import com.curso.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final RestaurantTableMapper tableMapper;

    @Transactional
    public RestaurantTableResponse create(RestaurantTableRequest request) {
        if (tableRepository.existsByNumber(request.getNumero())) {
            throw new BusinessRuleException("Ya existe una mesa con el numero " + request.getNumero());
        }

        RestaurantTable table = tableMapper.toEntity(request);
        return tableMapper.toResponse(tableRepository.save(table));
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantTableResponse> findById(Integer id) {
        return tableRepository.findById(id).map(tableMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> findAll() {
        return tableRepository.findAll().stream().map(tableMapper::toResponse).toList();
    }

    @Transactional
    public Optional<RestaurantTableResponse> update(Integer id, RestaurantTableRequest request) {
        Optional<RestaurantTable> found = tableRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        if (tableRepository.existsByNumberAndIdNot(request.getNumero(), id)) {
            throw new BusinessRuleException("Ya existe otra mesa con el numero " + request.getNumero());
        }

        RestaurantTable table = found.get();
        table.setNumber(request.getNumero());
        table.setCapacity(request.getCapacidad());
        table.setLocation(request.getUbicacion());
        if (request.getActiva() != null) {
            table.setActive(request.getActiva());
        }

        return Optional.of(tableMapper.toResponse(tableRepository.save(table)));
    }

    @Transactional
    public boolean delete(Integer id) {
        if (!tableRepository.existsById(id)) {
            return false;
        }

        tableRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> findAvailable(LocalDateTime dateTime, Integer partySize) {

        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora deben ser futuras");
        }

        if (partySize == null || partySize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de personas debe ser mayor que cero");
        }

        // Todas las reservas duran inicialmente dos horas.
        LocalDateTime startTime = dateTime.minusHours(2);
        LocalDateTime endTime = dateTime.plusHours(2);

        return tableRepository.findAvailable(partySize, startTime, endTime, List.of(ReservationStatus.PENDIENTE, ReservationStatus.CONFIRMADA)).
                stream().map(tableMapper::toResponse).toList();
    }
}
