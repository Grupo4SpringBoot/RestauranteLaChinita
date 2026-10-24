package com.curso.repository;

import com.curso.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface App_userRepository extends JpaRepository<AppUser, Integer> {
    // Buscar un usuario por su nombre (clave para el login)
    Optional<AppUser> findByUsername(String username);
}
