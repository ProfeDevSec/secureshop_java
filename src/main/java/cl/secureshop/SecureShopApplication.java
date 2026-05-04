package cl.secureshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SecureShop — Portal de Gestión Comercial
 * Aplicación educativa para curso de Desarrollo Seguro
 */
@SpringBootApplication
public class SecureShopApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(SecureShopApplication.class, args);
        // Verificar que los controllers están registrados
        var mapping = ctx.getBean(
                org.springframework.web.servlet.mvc.method.annotation
                        .RequestMappingHandlerMapping.class);
        mapping.getHandlerMethods().forEach((k, v) ->
                System.out.println("MAPPING: " + k + " -> " + v));
    }
}
