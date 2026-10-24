//Sirve para enviarle al cliente los datos del producto ya procesados
// con su respectivo ID de la base de datos.

package com.curso.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductoResponse {
    private Integer id;
    private String nombre;
    private BigDecimal precio;
    private String tipo;
    private boolean activo;
}
