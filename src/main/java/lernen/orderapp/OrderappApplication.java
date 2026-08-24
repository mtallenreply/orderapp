package lernen.orderapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Orderapp API",
                version = "1.0",
                description = "Verwaltung von Bestellungen, Kunden und Statistiken."
        )
)
@SpringBootApplication
public final  class OrderappApplication {

    static void main(final String[] args) {
        SpringApplication.run(OrderappApplication.class, args);
    }

}
