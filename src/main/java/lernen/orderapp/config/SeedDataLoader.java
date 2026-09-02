package lernen.orderapp.config;

import lernen.orderapp.entity.Customer;
import lernen.orderapp.entity.CustomerType;
import lernen.orderapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public final class SeedDataLoader implements CommandLineRunner {
    private final CustomerRepository customerRepository;

    @Override
    public void run(final String... args) {


        try (final Reader in = new InputStreamReader(new ClassPathResource("beispiel-kunden.csv").getInputStream(), StandardCharsets.UTF_8)) {
            final CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(in);

            for (final CSVRecord csvrecord : parser) {
                final String loyalty = csvrecord.get("loyaltyDiscountPercent");
                final BigDecimal loyaltyDiscountPercent = (loyalty != null && !loyalty.isBlank())
                        ? BigDecimal.valueOf(Double.parseDouble(loyalty)) : null;
                final Customer customer = new Customer(csvrecord.get("customerId"),
                        CustomerType.valueOf(csvrecord.get("customerType")), loyaltyDiscountPercent);
                customerRepository.save(customer);

            }

        } catch (final Exception e) {
            log.error("result = {}", e.getMessage());
        }
    }
}
