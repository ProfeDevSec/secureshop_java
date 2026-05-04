package cl.secureshop.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Registro de auditoría del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {
    private Long id;
    private String nivel;
    private String mensaje;
    private String usuario;
    private String ip;
    private String createdAt;
}
