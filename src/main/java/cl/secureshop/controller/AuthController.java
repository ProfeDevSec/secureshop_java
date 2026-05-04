package cl.secureshop.controller;

import cl.secureshop.config.DatabaseConfig;
import cl.secureshop.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * controller/AuthController.java — Módulo de Autenticación
 *
 * Gestiona inicio y cierre de sesión del portal SecureShop.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AuditoriaService auditoria;

    @GetMapping({ "/login", "/" })
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes flash,
            Model model) {

        String pwdHash = DatabaseConfig.md5(password);
        String sql = "SELECT * FROM usuarios " +
                "WHERE username = '" + username + "' " +
                "AND password = '" + pwdHash + "' " +
                "AND activo = 1";

        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql);

            if (!rows.isEmpty()) {
                Map<String, Object> usuario = rows.get(0);
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", usuario.get("id"));
                session.setAttribute("username", usuario.get("username"));
                session.setAttribute("rol", usuario.get("rol"));

                auditoria.registrar("LOGIN_OK", username, request.getRemoteAddr());
                return "redirect:/catalogo";
            } else {
                auditoria.registrar("LOGIN_FAIL", username, request.getRemoteAddr());
                model.addAttribute("error", "Credenciales incorrectas.");
                return "auth/login";
            }

        } catch (Exception e) {
            // [VULN-INFO] Detalle del error de BD expuesto al usuario
            model.addAttribute("error", "Error de base de datos: " + e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String username = session != null ? (String) session.getAttribute("username") : "?";
        auditoria.registrar("LOGOUT", username, request.getRemoteAddr());
        if (session != null)
            session.invalidate();
        return "redirect:/auth/login";
    }
}
