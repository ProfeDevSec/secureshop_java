package cl.secureshop.controller;

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
 * controller/AdminController.java — Panel de Administración
 *
 * Gestión de usuarios, estadísticas y configuración del sistema.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private AuditoriaService auditoria;

    private boolean esAdmin(HttpSession session) {
        return "admin".equals(session.getAttribute("rol"));
    }

    @GetMapping({"", "/"})
    public String index(HttpServletRequest request, Model model) {
        if (!esAdmin(request.getSession())) return "redirect:/catalogo";

        Map<String, Object> stats = Map.of(
            "usuarios",  jdbc.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class),
            "productos", jdbc.queryForObject("SELECT COUNT(*) FROM productos WHERE activo=1", Integer.class),
            "pedidos",   jdbc.queryForObject("SELECT COUNT(*) FROM pedidos", Integer.class),
            "resenas",   jdbc.queryForObject("SELECT COUNT(*) FROM resenas", Integer.class)
        );
        model.addAttribute("stats", stats);
        return "admin/index";
    }

    @GetMapping("/usuarios")
    public String usuarios(HttpServletRequest request, Model model) {
        if (!esAdmin(request.getSession())) return "redirect:/catalogo";

        List<Map<String, Object>> usuarios = jdbc.queryForList(
            "SELECT * FROM usuarios ORDER BY id");
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{uid}/toggle")
    public String toggleUsuario(
            @PathVariable Long uid,
            HttpServletRequest request,
            RedirectAttributes flash) {

        if (!esAdmin(request.getSession())) return "redirect:/catalogo";

        // [VULN-CSRF] No hay token CSRF — cualquier sitio puede enviar este POST
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT * FROM usuarios WHERE id = ?", uid);
        if (!rows.isEmpty()) {
            Map<String, Object> u = rows.get(0);
            int nuevoEstado = ((Number) u.get("activo")).intValue() == 1 ? 0 : 1;
            jdbc.update("UPDATE usuarios SET activo = ? WHERE id = ?", nuevoEstado, uid);

            String actor = (String) request.getSession().getAttribute("username");
            auditoria.registrar("USUARIO_TOGGLE", actor, request.getRemoteAddr());
            flash.addFlashAttribute("exito", "Usuario actualizado.");
        }
        return "redirect:/admin/usuarios";
    }
}
