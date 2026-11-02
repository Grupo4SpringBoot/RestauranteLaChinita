package com.curso.controller;

import com.curso.config.SecurityConfig;
import com.curso.dto.OrderResponse;
import com.curso.dto.ReservationResponse;
import com.curso.exception.GlobalExceptioneHandler;
import com.curso.repository.OrderRepository;
import com.curso.repository.ReservationRepository;
import com.curso.security.AppUserDetailsService;
import com.curso.security.OwnershipSecurity;
import com.curso.service.OrderService;
import com.curso.service.ReservationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercises the real HTTP filters, JWT converter, method security and ownership checks. */
@SpringJUnitConfig(ApiSecurityTest.TestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = {
        "app.jwt.secret=unit-test-only-key-with-at-least-32-bytes",
        "app.jwt.issuer=restaurant-test"
})
class ApiSecurityTest {

    private static final String EMAIL = "client@example.com";
    private static final String SECRET = "unit-test-only-key-with-at-least-32-bytes";

    @Autowired private WebApplicationContext context;
    @Autowired private FilterChainProxy securityFilterChain;
    @Autowired private ReservationService reservationService;
    @Autowired private OrderService orderService;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private OrderRepository orderRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(reservationService, orderService, reservationRepository, orderRepository);
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(securityFilterChain).build();
    }

    @Test
    void shouldReturnUnauthorizedForReservationWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/reservas/1"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(reservationService, reservationRepository);
    }

    @Test
    void shouldReturnUnauthorizedForOrderWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/pedidos/1"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(orderService, orderRepository);
    }

    @Test
    void shouldRejectInvalidReservationRequest() throws Exception {
        mvc.perform(post("/api/v1/reservas")
                        .header(HttpHeaders.AUTHORIZATION, bearer("reserva:crear"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mesaId": 1, "numPersonas": 0,
                                 "fechaHora": "2000-01-01T12:00:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(reservationService);
    }

    @Test
    void shouldRejectInvalidOrderRequest() throws Exception {
        mvc.perform(post("/api/v1/pedidos")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearer("pedido:crear", "pedido:gestionar"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reservationId": 1,
                                 "items": [{"productId": 10, "quantity": 0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(orderService);
    }

    @Test
    void shouldDenyAccessToAnotherUsersReservation() throws Exception {
        when(reservationRepository.existsByIdAndUser_Email(1, EMAIL)).thenReturn(false);
        mvc.perform(get("/api/v1/reservas/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("reserva:ver")))
                .andExpect(status().isForbidden());
        verify(reservationRepository).existsByIdAndUser_Email(1, EMAIL);
        verifyNoInteractions(reservationService);
    }

    @Test
    void shouldAllowAccessToOwnReservation() throws Exception {
        when(reservationRepository.existsByIdAndUser_Email(1, EMAIL)).thenReturn(true);
        when(reservationService.findById(1))
                .thenReturn(Optional.of(new ReservationResponse()));
        mvc.perform(get("/api/v1/reservas/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("reserva:ver")))
                .andExpect(status().isOk());
        verify(reservationService).findById(1);
    }

    @Test
    void shouldDenyAccessToAnotherUsersOrder() throws Exception {
        when(orderRepository.existsByIdAndReservation_User_Email(1, EMAIL)).thenReturn(false);
        mvc.perform(get("/api/v1/pedidos/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("pedido:ver")))
                .andExpect(status().isForbidden());
        verify(orderRepository).existsByIdAndReservation_User_Email(1, EMAIL);
        verifyNoInteractions(orderService);
    }

    @Test
    void shouldAllowAccessToOwnOrder() throws Exception {
        when(orderRepository.existsByIdAndReservation_User_Email(1, EMAIL)).thenReturn(true);
        when(orderService.getOrderById(1)).thenReturn(new OrderResponse());
        mvc.perform(get("/api/v1/pedidos/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("pedido:ver")))
                .andExpect(status().isOk());
        verify(orderService).getOrderById(1);
    }

    @Test
    void shouldDenyCreatingOrderForAnotherUsersReservation() throws Exception {
        when(reservationRepository.existsByIdAndUser_Email(1, EMAIL)).thenReturn(false);
        mvc.perform(post("/api/v1/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer("pedido:crear"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reservationId": 1,
                                 "items": [{"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isForbidden());
        verify(reservationRepository).existsByIdAndUser_Email(1, EMAIL);
        verifyNoInteractions(orderService);
    }

    @Test
    void shouldDenyUpdatingAnotherUsersOrder() throws Exception {
        when(orderRepository.existsByIdAndReservation_User_Email(1, EMAIL)).thenReturn(false);
        mvc.perform(put("/api/v1/pedidos/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("pedido:crear"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": 10, "quantity": 2}]}
                                """))
                .andExpect(status().isForbidden());
        verify(orderRepository).existsByIdAndReservation_User_Email(1, EMAIL);
        verifyNoInteractions(orderService);
    }

    private String bearer(String... permissions) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(EMAIL).issuer("restaurant-test")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .claim("permisos", List.of(permissions))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        return "Bearer " + token;
    }

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, OwnershipSecurity.class,
            ReservationController.class, OrderController.class, GlobalExceptioneHandler.class})
    static class TestConfig {
        @Bean ReservationService reservationService() { return mock(ReservationService.class); }
        @Bean OrderService orderService() { return mock(OrderService.class); }
        @Bean ReservationRepository reservationRepository() { return mock(ReservationRepository.class); }
        @Bean OrderRepository orderRepository() { return mock(OrderRepository.class); }
        @Bean AppUserDetailsService userDetailsService() { return mock(AppUserDetailsService.class); }
    }
}
