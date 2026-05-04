package cl.secureshop.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Entidad de negocio: Pedido comercial.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    private Long id;
    private Long usuarioId;
    private String username;
    private String email;
    private String estado;
    private Double total;
    private String notas;
    private String createdAt;
    private List<PedidoItem> items;
}
