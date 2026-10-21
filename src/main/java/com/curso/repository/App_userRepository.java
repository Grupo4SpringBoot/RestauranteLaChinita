package com.curso.repository;

import com.curso.model.App_user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface App_userRepository extends JpaRepository<App_user, Integer> {
    // Buscar un usuario por su nombre (clave para el login)
    Optional<App_user> findByUsername(String username);
}
