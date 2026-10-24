
package com.curso.controller;

import com.curso.dto.ProductoRequest;
import com.curso.dto.ProductoResponse;
import com.curso.model.Producto;
import com.curso.repository.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> createProducto(@Valid @RequestBody ProductoRequest request) {
        // 1. Convertir DTO Request a Entidad física
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setTipo(request.getTipo());
        producto.setActivo(true); // Valor por defecto local

        Producto nuevoProducto = productoRepository.save(producto);

        // 2. Convertir la Entidad guardada a DTO Response
        ProductoResponse response = new ProductoResponse();
        response.setId(nuevoProducto.getId());
        response.setNombre(nuevoProducto.getNombre());
        response.setPrecio(nuevoProducto.getPrecio());
        response.setTipo(nuevoProducto.getTipo());
        response.setActivo(nuevoProducto.isActivo());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> getAllProductos() {
        List<ProductoResponse> responseList = productoRepository.findAll().stream()
                .map(producto -> {
                    ProductoResponse res = new ProductoResponse();
                    res.setId(producto.getId());
                    res.setNombre(producto.getNombre());
                    res.setPrecio(producto.getPrecio());
                    res.setTipo(producto.getTipo());
                    res.setActivo(producto.isActivo());
                    return res;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
