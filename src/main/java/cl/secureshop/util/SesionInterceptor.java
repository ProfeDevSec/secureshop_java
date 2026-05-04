package cl.secureshop.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * util/SesionInterceptor.java — Guarda de sesión para rutas protegidas.
 *
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Solo interceptar handlers MVC reales, no recursos estáticos
        if (!(handler instanceof org.springframework.web.method.HandlerMethod)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }
        return true;
    }
}
