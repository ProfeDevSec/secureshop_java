package cl.secureshop.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Reseña de cliente sobre un producto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resena {
    private Long id;
    private Long productoId;
    private Long usuarioId;
    private String username;
    private String comentario;  // [VULN-XSS-02] almacenado sin sanitizar
    private Integer puntuacion;
    private String createdAt;
}
