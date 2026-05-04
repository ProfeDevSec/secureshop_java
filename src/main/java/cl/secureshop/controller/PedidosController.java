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

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * controller/PedidosController.java — Módulo de Gestión de Pedidos
 *
 * Creación, listado y detalle de pedidos comerciales.
 */
@Controller
@RequestMapping("/pedidos")
public class PedidosController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private AuditoriaService auditoria;

    /* ── Lista de pedidos ───────────────────────────────────────────────────── */
    @GetMapping({"", "/"})
    public String lista(
            @RequestParam(required = false, defaultValue = "") String prefs,
            HttpServletRequest request,
            Model model) {

        PreferenciasVista vistaPrefs = new PreferenciasVista();
        if (!prefs.isEmpty()) {
            try {
                byte[] data = Base64.getDecoder().decode(prefs);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                vistaPrefs = (PreferenciasVista) ois.readObject();   // ← RCE aquí
            } catch (Exception e) {
                // Silenciar error — comportamiento por defecto
            }
        }

        HttpSession session = request.getSession();
        String rol    = (String) session.getAttribute("rol");
        Long   userId = session.getAttribute("userId") != null ? ((Number) session.getAttribute("userId")).longValue() : null;

        List<Map<String, Object>> pedidos;
        if (List.of("admin", "vendedor", "auditor").contains(rol)) {
            pedidos = jdbc.queryForList("""
                SELECT p.*, u.username FROM pedidos p
                JOIN usuarios u ON p.usuario_id = u.id
                ORDER BY p.created_at DESC""");
        } else {
            pedidos = jdbc.queryForList(
                "SELECT * FROM pedidos WHERE usuario_id = ? ORDER BY created_at DESC", userId);
        }

        String prefsToken = PreferenciasVista.generateDefaultToken();
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("prefs", vistaPrefs);
        model.addAttribute("prefsToken", prefsToken);
        return "orders/lista";
    }

    /* ── Nuevo pedido ───────────────────────────────────────────────────────── */
    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        List<Map<String, Object>> productos = jdbc.queryForList(
            "SELECT id, nombre, precio, stock FROM productos WHERE activo = 1 AND stock > 0");
        model.addAttribute("productos", productos);
        model.addAttribute("prefsToken", PreferenciasVista.generateDefaultToken());
        return "orders/nuevo";
    }

    @PostMapping("/nuevo")
    public String crearPedido(
            @RequestParam Long productoId,
            @RequestParam(defaultValue = "1") Integer cantidad,
            @RequestParam(defaultValue = "") String notas,
            HttpServletRequest request,
            RedirectAttributes flash) {

        Long userId = ((Number) request.getSession().getAttribute("userId")).longValue();
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT * FROM productos WHERE id = ?", productoId);

        if (rows.isEmpty()) {
            flash.addFlashAttribute("error", "Producto no valido.");
            return "redirect:/pedidos/nuevo";
        }

        Map<String, Object> producto = rows.get(0);
        double total = ((Number) producto.get("precio")).doubleValue() * cantidad;

        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(conn -> {
            var ps = conn.prepareStatement(
                "INSERT INTO pedidos (usuario_id, estado, total, notas) VALUES (?, 'pendiente', ?, ?)",
                new String[]{"id"});
            ps.setLong(1, userId);
            ps.setDouble(2, total);
            ps.setString(3, notas);
            return ps;
        }, keyHolder);

        long pedidoId = keyHolder.getKey().longValue();
        jdbc.update(
            "INSERT INTO pedido_items (pedido_id, producto_id, cantidad, precio_unit) VALUES (?, ?, ?, ?)",
            pedidoId, productoId, cantidad, producto.get("precio")
        );

        String username = (String) request.getSession().getAttribute("username");
        auditoria.registrar("PEDIDO_CREADO", username, request.getRemoteAddr());
        flash.addFlashAttribute("exito", "Pedido #" + pedidoId + " creado exitosamente.");
        return "redirect:/pedidos/";
    }

    /* ── Detalle de pedido ──────────────────────────────────────────────────── */
    @GetMapping("/{pedidoId}")
    public String detalle(@PathVariable Long pedidoId,
                          HttpServletRequest request,
                          Model model) {

        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT p.*, u.username, u.email
            FROM pedidos p JOIN usuarios u ON p.usuario_id = u.id
            WHERE p.id = ?""", pedidoId);

        if (rows.isEmpty()) return "redirect:/pedidos/";

        List<Map<String, Object>> items = jdbc.queryForList("""
            SELECT pi.*, pr.nombre, pr.sku FROM pedido_items pi
            JOIN productos pr ON pi.producto_id = pr.id
            WHERE pi.pedido_id = ?""", pedidoId);

        model.addAttribute("pedido", rows.get(0));
        model.addAttribute("items", items);
        return "orders/detalle";
    }

    /* ── Clase interna serializable ─────────────── */

    /**
     * PreferenciasVista — almacena preferencias de visualización del usuario.
     *
     */
    public static class PreferenciasVista implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public int itemsPorPagina  = 10;
        public String orden        = "desc";
        public boolean mostrarCompletados = true;

        public static String generateDefaultToken() {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new ObjectOutputStream(baos).writeObject(new PreferenciasVista());
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch (Exception e) {
                return "";
            }
        }
    }
}
