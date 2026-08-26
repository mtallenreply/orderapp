package lernen.orderapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public final class StartupUrlLogger {

    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupUrls() {
        final String port = environment.getProperty("local.server.port", "8080");
        final String contextPath = environment.getProperty("server.servlet.context-path", "");
        final String baseUrl = "http://localhost:" + port + contextPath;

        log.info("Orderapp gestartet - erreichbar unter:");
        log.info("  Swagger UI:     {}/swagger-ui/index.html", baseUrl);
        log.info("  OpenAPI JSON:   {}/v3/api-docs", baseUrl);
        log.info("  H2 Console:     {}/h2-console", baseUrl);
    }
}