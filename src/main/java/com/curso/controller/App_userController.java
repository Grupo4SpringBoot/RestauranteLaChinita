package com.curso.controller;

import com.curso.model.App_user;
import com.curso.repository.App_userRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class App_userController {

    private final App_userRepository userRepository;

    // Inyección por constructor (Buena práctica)
    public App_userController(App_userRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET: Obtener todos los usuarios
    @GetMapping
    public List<App_user> getAllUsuarios() {
        return userRepository.findAll();
    }

    // POST: Registrar un nuevo usuario (Cumpliendo con el alta de clientes)
    @PostMapping
    public ResponseEntity<App_user> createUsuario(@RequestBody App_user usuario) {
        // Nota: En producción, aquí se debería aplicar el hash a la contraseña
        App_user nuevoUsuario = userRepository.save(usuario);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }
}
