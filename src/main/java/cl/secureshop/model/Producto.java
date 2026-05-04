package cl.secureshop.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad de negocio: Producto del catálogo SecureShop.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String categoria;
    private boolean activo;
}
