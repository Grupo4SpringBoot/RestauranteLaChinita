package com.curso.controller;

import com.curso.dto.UserRegistrationRequest;
import com.curso.dto.UserResponse;
import com.curso.model.AppUser; // Sincronizado con tu nuevo nombre de clase
import com.curso.repository.AppUserRepository; // Sincronizado con tu nuevo repositorio
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class AppUserController { // Nombre físico del archivo actual

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // El constructor ahora se llama exactamente igual que la clase del archivo
    public AppUserController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. REGISTRO (Guarda la contraseña en Hash BCrypt y asigna Rol CLIENTE de forma unificada)
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
        }

        AppUser usuario = new AppUser(); // Usando tu nueva clase
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);

        AppUser guardado = userRepository.save(usuario);
        return new ResponseEntity<>(mapToResponse(guardado), HttpStatus.CREATED);
    }

    // 2. INICIO DE SESIÓN (Establece el usuario logueado en el motor interno de Spring Security)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        return userRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    Authentication auth = new UsernamePasswordAuthenticationToken(u.getUsername(), null, null);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    return ResponseEntity.ok(Map.of("mensaje", "Inicio de sesión correcto", "usuario", u.getUsername()));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales incorrectas")));
    }

    // 3. CONSULTAR PERFIL (Protección total: Bloquea el acceso si Juan intenta ver el perfil de Maria)
    @GetMapping("/perfil/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<?> verPerfil(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(u -> ResponseEntity.ok(mapToResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. EDITAR PERFIL (Protección total: Impide modificar información de otros usuarios)
    @PutMapping("/perfil/{username}")
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<?> editarPerfil(@PathVariable String username, @RequestBody Map<String, String> updates) {
        return userRepository.findByUsername(username)
                .map(usuario -> {
                    if (updates.containsKey("email")) usuario.setEmail(updates.get("email"));
                    if (updates.containsKey("password")) {
                        usuario.setPassword(passwordEncoder.encode(updates.get("password")));
                    }
                    usuario.setRol("CLIENTE"); // Rol unificado

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

