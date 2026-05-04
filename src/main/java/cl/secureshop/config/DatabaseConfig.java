package cl.secureshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * config/DatabaseConfig.java — Inicialización y seed de la base de datos SQLite.
 * Equivalente a models/database.py de la versión Flask.
 */
@Configuration
public class DatabaseConfig {

    @Autowired
    private JdbcTemplate jdbc;

    @PostConstruct
    public void initDatabase() {
        new File("./instance").mkdirs();
        createTables();
        seedData();
    }

    private void createTables() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS usuarios (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                username   TEXT NOT NULL UNIQUE,
                password   TEXT NOT NULL,
                email      TEXT NOT NULL,
                rol        TEXT NOT NULL DEFAULT 'cliente',
                activo     INTEGER NOT NULL DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )""");

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS productos (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                sku         TEXT NOT NULL UNIQUE,
                nombre      TEXT NOT NULL,
                descripcion TEXT,
                precio      REAL NOT NULL,
                stock       INTEGER NOT NULL DEFAULT 0,
                categoria   TEXT,
                activo      INTEGER NOT NULL DEFAULT 1
            )""");

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS pedidos (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                estado     TEXT NOT NULL DEFAULT 'pendiente',
                total      REAL NOT NULL DEFAULT 0,
                notas      TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )""");

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS pedido_items (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                pedido_id   INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad    INTEGER NOT NULL,
                precio_unit REAL NOT NULL,
                FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            )""");

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS resenas (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                producto_id INTEGER NOT NULL,
                usuario_id  INTEGER NOT NULL,
                comentario  TEXT,
                puntuacion  INTEGER,
                created_at  TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (producto_id) REFERENCES productos(id),
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )""");

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS system_logs (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                nivel      TEXT,
                mensaje    TEXT,
                usuario    TEXT,
                ip         TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )""");
    }

    private void seedData() {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM usuarios", Integer.class);
        if (count != null && count > 0) return;   // ya sembrado

        // [VULN-INFO] MD5 sin salt — deliberadamente inseguro
        Object[][] usuarios = {
            {"admin",    md5("Admin2024!"),    "admin@secureshop.cl",    "admin"},
            {"jperez",   md5("Comercial#99"),  "j.perez@secureshop.cl",  "vendedor"},
            {"mlopez",   md5("Lopez2023"),     "m.lopez@secureshop.cl",  "vendedor"},
            {"cliente1", md5("pass123"),       "cliente1@ejemplo.cl",    "cliente"},
            {"cliente2", md5("miClave456"),    "cliente2@ejemplo.cl",    "cliente"},
            {"auditor",  md5("Audit0r!"),      "auditor@secureshop.cl",  "auditor"},
        };
        for (Object[] u : usuarios) {
            jdbc.update("INSERT OR IGNORE INTO usuarios (username,password,email,rol) VALUES (?,?,?,?)", u);
        }

        Object[][] productos = {
            {"SKU-001","Laptop ProBook 15",       "Laptop empresarial 15\" Intel i7",      899990.0, 25, "Computacion"},
            {"SKU-002","Monitor UltraWide 34\"",  "Monitor curvo 3440x1440 144Hz",         449990.0, 12, "Perifericos"},
            {"SKU-003","Teclado Mecanico TKL",    "Switch Cherry MX Red, retroiluminado",   79990.0, 50, "Perifericos"},
            {"SKU-004","Mouse Ergonomico Pro",    "Disenio ergonomico, 6 botones",          34990.0, 80, "Perifericos"},
            {"SKU-005","Hub USB-C 7 en 1",        "HDMI 4K, USB 3.0, SD, PD 100W",         29990.0, 60, "Accesorios"},
            {"SKU-006","SSD NVMe 1TB",            "PCIe Gen4, 7000MB/s lectura",           119990.0, 35, "Almacenamiento"},
            {"SKU-007","Auriculares ANC Pro",     "Cancelacion activa de ruido, BT 5.2",   89990.0, 20, "Audio"},
            {"SKU-008","Webcam 4K Stream",        "Autoenfoque, microfono dual stereo",     69990.0, 15, "Video"},
            {"SKU-009","Silla Ergonomica Mesh",   "Soporte lumbar ajustable, 8h uso",      299990.0,  8, "Mobiliario"},
            {"SKU-010","Dock Station Thunderbolt","2x TB4, 2.5G Ethernet, 96W PD",        199990.0, 10, "Accesorios"},
        };
        for (Object[] p : productos) {
            jdbc.update(
                "INSERT OR IGNORE INTO productos (sku,nombre,descripcion,precio,stock,categoria) VALUES (?,?,?,?,?,?)", p);
        }

        jdbc.update("INSERT OR IGNORE INTO pedidos (id,usuario_id,estado,total,notas) VALUES (1,4,'completado',979980,'Entrega en oficina')");
        jdbc.update("INSERT OR IGNORE INTO pedido_items (pedido_id,producto_id,cantidad,precio_unit) VALUES (1,1,1,899990)");
        jdbc.update("INSERT OR IGNORE INTO pedido_items (pedido_id,producto_id,cantidad,precio_unit) VALUES (1,4,1,79990)");
        jdbc.update("INSERT OR IGNORE INTO pedidos (id,usuario_id,estado,total,notas) VALUES (2,5,'pendiente',449990,NULL)");
        jdbc.update("INSERT OR IGNORE INTO pedido_items (pedido_id,producto_id,cantidad,precio_unit) VALUES (2,2,1,449990)");

        jdbc.update("INSERT OR IGNORE INTO resenas (id,producto_id,usuario_id,comentario,puntuacion) VALUES (1,1,4,'Excelente laptop, muy rapida para el trabajo.',5)");
        jdbc.update("INSERT OR IGNORE INTO resenas (id,producto_id,usuario_id,comentario,puntuacion) VALUES (2,2,5,'El monitor es espectacular, colores increibles.',5)");
    }

    // [VULN-INFO] MD5 — deliberadamente inseguro para el ejercicio educativo
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
