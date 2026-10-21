
package com.curso.controller;

import com.curso.model.Pedido;
import com.curso.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;

    public PedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // POST: Crear un nuevo pedido con múltiples líneas de detalle
    @PostMapping
    public ResponseEntity<Pedido> createPedido(@RequestBody Pedido pedido) {
        Pedido nuevoPedido = pedidoRepository.save(pedido);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    // GET: Recuperar los detalles de un pedido específico filtrando por el ID de la orden
    @GetMapping("/orden/{orderId}")
    public ResponseEntity<List<Pedido>> getPedidosByOrden(@PathVariable int orderId) {
        List<Pedido> detalles = pedidoRepository.findByOrderId(orderId);
        return ResponseEntity.ok(detalles);
    }
}
