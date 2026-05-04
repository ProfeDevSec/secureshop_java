package cl.secureshop.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Ítem de línea dentro de un pedido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItem {
    private Long id;
    private Long pedidoId;
    private Long productoId;
    private String productoNombre;
    private String sku;
    private Integer cantidad;
    private Double precioUnit;
}
