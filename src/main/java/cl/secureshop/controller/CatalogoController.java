package cl.secureshop.controller;

import cl.secureshop.model.Producto;
import cl.secureshop.model.Resena;
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
import java.util.stream.Collectors;

/**
 * controller/CatalogoController.java — Módulo de Catálogo de Productos
 *
 * Búsqueda, visualización y sistema de reseñas de productos.
 */
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private AuditoriaService auditoria;

    /* ── Índice del catálogo ────────────────────────────────────────────────── */
    @GetMapping({"", "/"})
    public String index(@RequestParam(required = false, defaultValue = "") String categoria,
                        Model model) {
        List<Map<String, Object>> productos = jdbc.queryForList(
            "SELECT * FROM productos WHERE activo = 1 ORDER BY categoria, nombre");
        List<Map<String, Object>> categorias = jdbc.queryForList(
            "SELECT DISTINCT categoria FROM productos WHERE activo = 1");

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriaActiva", categoria);
        return "catalog/index";
    }

    /* ── Búsqueda de productos ──────────────────────────────────────────────── */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false, defaultValue = "") String q,
                         Model model) {

        String sql = "SELECT * FROM productos WHERE activo = 1 " +
                     "AND (nombre LIKE '%" + q + "%' OR descripcion LIKE '%" + q + "%')";

        List<Map<String, Object>> productos;
        try {
            productos = jdbc.queryForList(sql);
        } catch (Exception e) {
            // [VULN-INFO] Mensaje de error con detalle SQL expuesto
            model.addAttribute("error", "Error en consulta: " + e.getMessage());
            productos = List.of();
        }

        String mensajeBusqueda = "Resultados para: <strong>" + q + "</strong>";

        model.addAttribute("productos", productos);
        model.addAttribute("termino", q);
        model.addAttribute("mensajeBusqueda", mensajeBusqueda);  // HTML crudo → th:utext
        return "catalog/buscar";
    }

    /* ── Detalle de producto ────────────────────────────────────────────────── */
    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT * FROM productos WHERE id = ? AND activo = 1", id);

        if (rows.isEmpty()) return "redirect:/catalogo";

        List<Map<String, Object>> resenas = jdbc.queryForList("""
            SELECT r.*, u.username FROM resenas r
            JOIN usuarios u ON r.usuario_id = u.id
            WHERE r.producto_id = ?
            ORDER BY r.created_at DESC""", id);

        model.addAttribute("producto", rows.get(0));
        model.addAttribute("resenas", resenas);
        return "catalog/detalle";
    }

    /* ── Agregar reseña ─────────────────────────────────────────────────────── */
    @PostMapping("/producto/{id}/resena")
    public String agregarResena(
            @PathVariable Long id,
            @RequestParam String comentario,
            @RequestParam(defaultValue = "3") Integer puntuacion,
            HttpServletRequest request,
            RedirectAttributes flash) {

        Long userId = ((Number) request.getSession().getAttribute("userId")).longValue();
        jdbc.update(
            "INSERT INTO resenas (producto_id, usuario_id, comentario, puntuacion) VALUES (?, ?, ?, ?)",
            id, userId, comentario, puntuacion
        );

        String username = (String) request.getSession().getAttribute("username");
        auditoria.registrar("RESENA_CREADA", username, request.getRemoteAddr());
        flash.addFlashAttribute("exito", "Reseña publicada correctamente.");
        return "redirect:/catalogo/producto/" + id;
    }
}
