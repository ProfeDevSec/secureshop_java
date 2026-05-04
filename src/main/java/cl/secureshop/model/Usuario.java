package cl.secureshop.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad de negocio: Usuario del sistema SecureShop.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String rol;        // admin | vendedor | cliente | auditor
    private boolean activo;
    private String createdAt;
}
