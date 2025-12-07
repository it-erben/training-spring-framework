package tech.erben.springboot;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class TomcatLauncher {

    @PostConstruct
    public void launch() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector().setProperty("address", "0.0.0.0");
        Context context = tomcat.addContext("", null);
        Tomcat.addServlet(context, "dateServlet", CurrentDateServlet.class.getName());
        context.addServletMappingDecoded("/", "dateServlet");
        tomcat.start();
        tomcat.getServer().await();
    }

    public static class CurrentDateServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
            resp.getWriter().write("Current date: " + LocalDateTime.now());
        }
    }
}
