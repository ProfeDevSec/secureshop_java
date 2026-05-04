# SecureShop Java — Aplicación Web Vulnerable (Educativa)

> **Curso de Desarrollo Seguro · Secure Develop SpA**
> Stack: Spring Boot 3.2.5 · Thymeleaf · SQLite · Java 21
> 
> Author: profedevsec@gmail.com

---

## Requisitos

| Herramienta | Versión mínima |
|-------------|----------------|
| JDK         | 21             |
| Maven       | 3.8+           |

---

## Ejecución

```bash
cd secureshop-java

# Ejecutar en modo desarrollo (la BD SQLite se crea en ./instance/)
mvn spring-boot:run

# O compilar fat JAR y ejecutar
mvn package -DskipTests
java -jar target/secureshop-1.0.0.jar
```

App disponible en: **http://localhost:8080**

---

## Credenciales de laboratorio

| Usuario    | Contraseña     | Rol       |
|------------|----------------|-----------|
| `admin`    | `Admin2024!`   | admin     |
| `jperez`   | `Comercial#99` | vendedor  |
| `mlopez`   | `Lopez2023`    | vendedor  |
| `cliente1` | `pass123`      | cliente   |
| `cliente2` | `miClave456`   | cliente   |
| `auditor`  | `Audit0r!`     | auditor   |

---

## Estructura del proyecto

```
secureshop-java/
├── pom.xml
└── src/main/
    ├── java/cl/secureshop/
    │   ├── SecureShopApplication.java
    │   ├── config/
    │   │   ├── DatabaseConfig.java     ← Schema SQLite + seed data
    │   │   └── WebConfig.java          ← Interceptor de sesión
    │   ├── util/
    │   │   └── SesionInterceptor.java
    │   ├── service/
    │   │   └── AuditoriaService.java
    │   ├── model/
    │   │   ├── Usuario.java
    │   │   ├── Producto.java
    │   │   ├── Pedido.java
    │   │   ├── PedidoItem.java
    │   │   ├── Resena.java
    │   │   └── SystemLog.java
    │   └── controller/
    │       ├── HomeController.java
    │       ├── AuthController.java     
    │       ├── CatalogoController.java  
    │       ├── PedidosController.java   
    │       ├── ReportesController.java  
    │       └── AdminController.java    
    └── resources/
        ├── application.properties
        └── templates/
            ├── layout.html              ← Layout base Thymeleaf
            ├── auth/login.html
            ├── catalog/{index,buscar,detalle}.html
            ├── orders/{lista,nuevo,detalle}.html
            ├── reports/{index,ventas,diagnostico,exportar}.html
            └── admin/{index,usuarios}.html
```

---

*Desarrollado para uso educativo interno · 2026*
