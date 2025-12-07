package tech.erben.springboot;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfiguration {

    @Bean
    public ServletRegistrationBean<OsInfoServlet> osInfoServlet(
        OperatingSystem operatingSystem
    ) {
        ServletRegistrationBean<OsInfoServlet> bean = new ServletRegistrationBean<>(
            new OsInfoServlet(operatingSystem),
            "/os"
        );
        bean.setLoadOnStartup(1);
        return bean;
    }

    public static class OsInfoServlet extends HttpServlet {

        private final OperatingSystem operatingSystem;

        public OsInfoServlet(OperatingSystem operatingSystem) {
            this.operatingSystem = operatingSystem;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            resp.setContentType("text/plain");
            resp.getWriter().println(operatingSystem.writeOsInfo());
        }
    }
}
