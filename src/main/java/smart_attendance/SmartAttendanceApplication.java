package smart_attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import smart_attendance.service.UserService;
import smart_attendance.servlet.DashboardServlet;
import smart_attendance.servlet.LoginServlet;
import smart_attendance.servlet.LogoutServlet;
import smart_attendance.servlet.RegisterServlet;

/**
 * Main Application Entry Point.
 * Registers all Phase 4 Java Servlets (RegisterServlet, LoginServlet,
 * LogoutServlet, DashboardServlet) on the embedded Tomcat web server.
 */
@SpringBootApplication
public class SmartAttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAttendanceApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean<RegisterServlet> registerServletBean(UserService userService) {
        return new ServletRegistrationBean<>(new RegisterServlet(userService), "/registerServlet");
    }

    @Bean
    public ServletRegistrationBean<LoginServlet> loginServletBean(UserService userService) {
        return new ServletRegistrationBean<>(new LoginServlet(userService), "/loginServlet");
    }

    @Bean
    public ServletRegistrationBean<LogoutServlet> logoutServletBean() {
        return new ServletRegistrationBean<>(new LogoutServlet(), "/logoutServlet");
    }

    @Bean
    public ServletRegistrationBean<DashboardServlet> dashboardServletBean() {
        return new ServletRegistrationBean<>(new DashboardServlet(), "/dashboardServlet");
    }
}
