
package com.curso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar la seguridad estricta @PreAuthorize en los controladores
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Motor de encriptación BCrypt para las contraseñas
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactivado para simplificar pruebas REST con Postman o Frontend
                .authorizeHttpRequests(auth -> auth
                        // Rutas que CUALQUIERA puede usar sin estar logueado (Registro, Login y Catálogo)
                        .requestMatchers("/api/v1/usuarios/registro", "/api/v1/usuarios/login", "/api/v1/productos/**").permitAll()
                        // Cualquier otra ruta del restaurante requiere que el usuario haya iniciado sesión obligatoriamente
                        .anyRequest().authenticated()
                )
                // Configuración del Cierre de Sesión (Logout) gestionado automáticamente por Spring Security
                .logout(logout -> logout
                        .logoutUrl("/api/v1/usuarios/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)) // Devuelve un estado 200 OK limpio
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                );

        return http.build();
    }
}
