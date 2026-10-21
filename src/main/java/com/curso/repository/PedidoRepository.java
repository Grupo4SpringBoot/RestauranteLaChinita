package com.curso.repository;

import com.curso.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    // Buscar todos los detalles asociados a una orden en específico
    List<Pedido> findByOrderId(int orderId);
}
