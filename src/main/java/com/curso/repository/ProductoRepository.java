package com.curso.repository;

import com.curso.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Método útil para buscar solo productos del catálogo que estén vigentes
    List<Producto> findByActivoTrue();
}
