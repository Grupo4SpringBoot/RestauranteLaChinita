//Lista o muestra los usuarios en el sistema
package com.curso.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private String rol;
    private boolean activo;
}
