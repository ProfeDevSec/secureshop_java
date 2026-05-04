package cl.secureshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * service/AuditoriaService.java — Servicio de registro de eventos.
 */
@Service
public class AuditoriaService {

    @Autowired
    private JdbcTemplate jdbc;

    public void registrar(String nivel, String usuario, String ip) {
        registrar(nivel, usuario, ip, "");
    }

    public void registrar(String nivel, String usuario, String ip, String mensaje) {
        try {
            jdbc.update(
                "INSERT INTO system_logs (nivel, mensaje, usuario, ip) VALUES (?, ?, ?, ?)",
                nivel, mensaje, usuario, ip
            );
        } catch (Exception ignored) {
            // No interrumpir el flujo principal si el log falla
        }
    }
}
