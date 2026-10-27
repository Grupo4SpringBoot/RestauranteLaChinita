package com.curso.config;

import com.curso.model.UserRolType;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
public class PermisosConfig {

    public static final String CATALOGO_VER = "catalogo:ver";
    public static final String CATALOGO_GESTIONAR = "catalogo:gestionar";
    public static final String PEDIDO_CREAR = "pedido:crear";
    public static final String PEDIDO_VER = "pedido:ver";
    public static final String PEDIDO_GESTIONAR = "pedido:gestionar";
    public static final String RESERVA_CREAR = "reserva:crear";
    public static final String RESERVA_VER = "reserva:ver";
    public static final String RESERVA_GESTIONAR = "reserva:gestionar";
    public static final String USUARIOS_GESTIONAR = "usuarios:gestionar";

    private final Map<UserRolType, Set<String>> permisosPorRol = Map.of(
            UserRolType.CLIENTE, Set.of(
                    CATALOGO_VER,
                    PEDIDO_CREAR, PEDIDO_VER,
                    RESERVA_CREAR, RESERVA_VER),

            UserRolType.EMPLEADO, Set.of(
                    CATALOGO_VER, CATALOGO_GESTIONAR,
                    PEDIDO_CREAR, PEDIDO_VER, PEDIDO_GESTIONAR,
                    RESERVA_CREAR, RESERVA_VER, RESERVA_GESTIONAR),

            UserRolType.ADMIN, Set.of(
                    CATALOGO_VER, CATALOGO_GESTIONAR,
                    PEDIDO_CREAR, PEDIDO_VER, PEDIDO_GESTIONAR,
                    RESERVA_CREAR, RESERVA_VER, RESERVA_GESTIONAR,
                    USUARIOS_GESTIONAR)
    );

    public Set<String> permisosDe(UserRolType rol) {
        return permisosPorRol.getOrDefault(rol, Set.of());
    }
}
