package lernen.orderapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupUrlLogger {

    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupUrls() {
        final String port = environment.getProperty("local.server.port", "8080");
        final String contextPath = environment.getProperty("server.servlet.context-path", "");
        final String baseUrl = "http://localhost:" + port + contextPath;

        System.out.println();
        System.out.println("Orderapp gestartet - erreichbar unter:");
        System.out.println("  Swagger UI:     " + baseUrl + "/swagger-ui/index.html");
        System.out.println("  OpenAPI JSON:   " + baseUrl + "/v3/api-docs");
        System.out.println("  H2 Console:     " + baseUrl + "/h2-console");
        System.out.println();
    }
}