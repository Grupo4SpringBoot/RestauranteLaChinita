package com.curso.service;

import com.curso.dto.ReservationRequest;
import com.curso.dto.ReservationResponse;
import com.curso.exception.BusinessRuleException;
import com.curso.mapper.ReservationMapper;
import com.curso.model.AppUser;
import com.curso.model.Reservation;
import com.curso.model.Order;
import com.curso.model.ReservationStatus;
import com.curso.model.RestaurantTable;
import com.curso.repository.AppUserRepository;
import com.curso.repository.ReservationRepository;
import com.curso.repository.RestaurantTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final String EMAIL = "cliente@example.com";

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        request = new ReservationRequest();
        request.setMesaId(1);
        request.setNumPersonas(2);
        request.setFechaHora(LocalDateTime.now().plusDays(1));
    }

    @Test
    void shouldRejectReservationWithPastDate() {
        request.setFechaHora(LocalDateTime.now().minusDays(1));

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(new AppUser()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.create(request, EMAIL)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode()
        );

        verify(reservationRepository, never()).save(any(Reservation.class));

        verifyNoInteractions(tableRepository, reservationMapper);
    }

    @Test
    void shouldRejectReservationExceedingTableCapacity() {
        request.setNumPersonas(5);

        RestaurantTable table = createTable(4);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new AppUser()));

        when(tableRepository.findByIdForUpdate(1)).thenReturn(Optional.of(table));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> reservationService.create(request, EMAIL)
        );

        assertTrue(exception.getMessage().contains("capacidad"));

        verify(reservationRepository, never()).save(any(Reservation.class));

        verifyNoInteractions(reservationMapper);
    }

    @Test
    void shouldRejectReservationWithOverlappingSchedule() {
        RestaurantTable table = createTable(4);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new AppUser()));

        when(tableRepository.findByIdForUpdate(1)).thenReturn(Optional.of(table));


        when(reservationRepository
                .existsByMesa_IdAndStatusInAndDateTimeGreaterThanAndDateTimeLessThan(
                        isNull(),
                        anyList(),
                        eq(request.getFechaHora().minusHours(2)),
                        eq(request.getFechaHora().plusHours(2))))
                .thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> reservationService.create(request, EMAIL)
        );

        assertTrue(exception.getMessage().contains("horario"));

        verify(reservationRepository, never()).save(any(Reservation.class));

        verifyNoInteractions(reservationMapper);
    }

    @Test
    void shouldCreateValidReservation() {
        AppUser user = new AppUser();
        RestaurantTable table = createTable(4);
        Reservation entity = new Reservation();
        ReservationResponse expected = new ReservationResponse();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(tableRepository.findByIdForUpdate(1)).thenReturn(Optional.of(table));
        when(reservationRepository
                .existsByMesa_IdAndStatusInAndDateTimeGreaterThanAndDateTimeLessThan(
                        isNull(), eq(List.of(ReservationStatus.PENDIENTE, ReservationStatus.CONFIRMADA)),
                        eq(request.getFechaHora().minusHours(2)),
                        eq(request.getFechaHora().plusHours(2))))
                .thenReturn(false);
        when(reservationMapper.toEntity(request, user, table)).thenReturn(entity);
        when(reservationRepository.save(entity)).thenReturn(entity);
        when(reservationMapper.toResponse(entity)).thenReturn(expected);

        assertSame(expected, reservationService.create(request, EMAIL));

        verify(reservationRepository).save(entity);
    }

    @Test
    void shouldRejectInactiveTable() {
        RestaurantTable table = createTable(4);
        table.setActive(false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new AppUser()));
        when(tableRepository.findByIdForUpdate(1)).thenReturn(Optional.of(table));

        assertThrows(BusinessRuleException.class,
                () -> reservationService.create(request, EMAIL));

        verifyNoInteractions(reservationRepository, reservationMapper);
    }

    @Test
    void shouldRejectDeletingReservationWithOrders() {
        Reservation reservation = new Reservation();
        reservation.getOrders().add(new Order());
        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        assertThrows(BusinessRuleException.class, () -> reservationService.delete(1));

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    private RestaurantTable createTable(int capacity) {
        RestaurantTable table = new RestaurantTable();
        table.setNumber(1);
        table.setCapacity(capacity);
        table.setActive(true);
        return table;
    }
}
