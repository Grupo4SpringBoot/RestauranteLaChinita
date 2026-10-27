package com.curso.repository;

import com.curso.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    // El login se identifica por email, que es la UNIQUE en la base de datos (el nombre puede repetirse)
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
