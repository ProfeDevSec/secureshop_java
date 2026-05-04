package cl.secureshop.config;

import cl.secureshop.util.SesionInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Configuración MVC: interceptores, recursos estáticos.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SesionInterceptor sesionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sesionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/auth/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error"
                );
        // Interceptor que expone requestURI

        registry.addInterceptor(new HandlerInterceptor() {
            public void postHandle(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object handler,
                                   ModelAndView modelAndView) {
                if (modelAndView != null) {
                    modelAndView.addObject("currentUri", request.getRequestURI());
                }
            }
        }).addPathPatterns("/**");
    }

    @Override
    public void configureDefaultServletHandling(
            DefaultServletHandlerConfigurer configurer) {
        // No llamar a configurer.enable() — así las rutas sin handler
        // generan 404 del DispatcherServlet, no del ResourceHandler
    }

    // mapeo de recursos estáticos
    // solo para rutas que realmente son estáticas
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        // NO agregar un handler para /** — eso causaría el problema
    }
}
