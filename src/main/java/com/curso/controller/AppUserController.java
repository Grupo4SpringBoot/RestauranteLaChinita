package com.curso.controller;

import com.curso.dto.LoginRequest;
import com.curso.dto.UserRegistrationRequest;
import com.curso.dto.UserResponse;
import com.curso.model.AppUser; // Sincronizado con tu nuevo nombre de clase
import com.curso.model.UserRolType;
import com.curso.repository.AppUserRepository; // Sincronizado con tu nuevo repositorio
import com.curso.security.AppUserDetails;
import com.curso.security.JwtTokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class AppUserController { // Nombre físico del archivo actual

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    // El constructor ahora se llama exactamente igual que la clase del archivo
    public AppUserController(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                             AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    // 1. REGISTRO (Guarda la contraseña en Hash BCrypt y asigna Rol CLIENTE de forma unificada)
    @SecurityRequirements
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
        }

        AppUser usuario = new AppUser(); // Usando tu nueva clase
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(UserRolType.CLIENTE);
        usuario.setActivo(true);

        AppUser guardado = userRepository.save(usuario);
        return new ResponseEntity<>(mapToResponse(guardado), HttpStatus.CREATED);
    }

    // 2. INICIO DE SESIÓN (Spring Security valida el hash, comprueba que la cuenta esté activa y crea el token con permisos)
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            AppUserDetails details = (AppUserDetails) authentication.getPrincipal();
            String token = jwtTokenService.generateToken(details);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "mensaje", "Inicio de sesión correcto",
                    "usuario", authentication.getName(),
                    "rol", details.getRol().name(),
                    "expiresIn", jwtTokenService.getExpirationSeconds()));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "La cuenta está desactivada"));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
        }
    }

    // 3. CONSULTAR PERFIL (Protección total: Bloquea el acceso si Juan intenta ver el perfil de Maria)
    @GetMapping("/perfil/{email}")
    @PreAuthorize("#email == authentication.name")
    public ResponseEntity<?> verPerfil(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(mapToResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. EDITAR PERFIL (Protección total: Impide modificar inf
    // rmación de otros usuarios)
    @PutMapping("/perfil/{email}")
    @PreAuthorize("#email == authentication.name")
    public ResponseEntity<?> editarPerfil(@PathVariable String email, @RequestBody Map<String, String> updates) {
        return userRepository.findByEmail(email)
                .map(usuario -> {
                    if (updates.containsKey("email")) usuario.setEmail(updates.get("email"));
                    if (updates.containsKey("password")) {
                        usuario.setPassword(passwordEncoder.encode(updates.get("password")));
                    }

                    AppUser actualizado = userRepository.save(usuario);
                    return ResponseEntity.ok(mapToResponse(actualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse mapToResponse(AppUser usuario) {
        UserResponse res = new UserResponse();
        res.setId(usuario.getId());
        res.setUsername(usuario.getUsername());
        res.setEmail(usuario.getEmail());
        res.setRol(usuario.getRol());
        res.setActivo(usuario.isActivo());
        return res;
    }
}

