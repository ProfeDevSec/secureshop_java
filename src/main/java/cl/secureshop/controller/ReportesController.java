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
 * controller/ReportesController.java — Módulo de Reportes Operacionales
 *
 * Reportes de ventas, diagnóstico de conectividad y exportación de logs.
 */
@Controller
@RequestMapping("/reportes")
public class ReportesController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private AuditoriaService auditoria;

    private boolean esAdminOVendedorOAuditor(HttpSession session) {
        String rol = (String) session.getAttribute("rol");
        return rol != null && List.of("admin", "vendedor", "auditor").contains(rol);
    }

    private boolean esAdmin(HttpSession session) {
        return "admin".equals(session.getAttribute("rol"));
    }

    @GetMapping({"", "/"})
    public String index(HttpServletRequest request) {
        if (!esAdminOVendedorOAuditor(request.getSession())) return "redirect:/catalogo";
        return "reports/index";
    }

    @GetMapping("/ventas")
    public String ventas(HttpServletRequest request, Model model) {
        if (!esAdminOVendedorOAuditor(request.getSession())) return "redirect:/catalogo";

        List<Map<String, Object>> resumen = jdbc.queryForList("""
            SELECT u.username, COUNT(p.id) as total_pedidos, SUM(p.total) as total_ventas
            FROM pedidos p JOIN usuarios u ON p.usuario_id = u.id
            GROUP BY u.username ORDER BY total_ventas DESC""");

        List<Map<String, Object>> detalle = jdbc.queryForList("""
            SELECT p.id, u.username, p.estado, p.total, p.created_at
            FROM pedidos p JOIN usuarios u ON p.usuario_id = u.id
            ORDER BY p.created_at DESC LIMIT 50""");

        model.addAttribute("resumen", resumen);
        model.addAttribute("detalle", detalle);
        return "reports/ventas";
    }

    /* ── Diagnóstico de red ─────────────────────────────────────────────────── */
    @GetMapping("/diagnostico")
    public String diagnosticoForm(HttpServletRequest request, Model model) {
        if (!esAdmin(request.getSession())) {
            return "redirect:/catalogo";
        }
        return "reports/diagnostico";
    }

    @PostMapping("/diagnostico")
    public String ejecutarDiagnostico(
            @RequestParam String host,
            HttpServletRequest request,
            Model model) {

        if (!esAdmin(request.getSession())) return "redirect:/catalogo";

        String resultado;
        try {
            // new String[]{"/bin/sh", "-c", cmd} — el shell interpreta operadores
            String cmd = "ping -c 2 " + host;
            Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});

            String stdout = new String(proc.getInputStream().readAllBytes());
            String stderr = new String(proc.getErrorStream().readAllBytes());
            proc.waitFor();
            resultado = stdout + (stderr.isEmpty() ? "" : "\n[stderr]\n" + stderr);

        } catch (Exception e) {
            resultado = "Error al ejecutar diagnóstico: " + e.getMessage();
        }

        String username = (String) request.getSession().getAttribute("username");
        auditoria.registrar("DIAGNOSTICO_EJECUTADO", username, request.getRemoteAddr());
        model.addAttribute("resultado", resultado);
        model.addAttribute("host", host);
        return "reports/diagnostico";
    }

    @GetMapping("/exportar")
    public String exportar(HttpServletRequest request, Model model) {
        if (!esAdmin(request.getSession()) &&
            !"auditor".equals(request.getSession().getAttribute("rol"))) {
            return "redirect:/catalogo";
        }
        List<Map<String, Object>> logs = jdbc.queryForList(
            "SELECT * FROM system_logs ORDER BY created_at DESC LIMIT 200");
        model.addAttribute("logs", logs);
        return "reports/exportar";
    }
}
