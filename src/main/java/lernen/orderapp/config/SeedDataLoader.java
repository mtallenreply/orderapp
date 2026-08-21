package lernen.orderapp.config;

import lernen.orderapp.entity.Customer;
import lernen.orderapp.entity.CustomerType;
import lernen.orderapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;


@Order(0)
@Component
@RequiredArgsConstructor
public class SeedDataLoader implements CommandLineRunner {
    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args)  {


        try (Reader in = Files.newBufferedReader(Path.of("src/main/resources/beispiel-kunden.csv"));){
                 final CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(in);

            for (CSVRecord csvrecord : parser) {
                final Customer customer = new Customer();
                customer.setId(csvrecord.get("customerId"));
                customer.setCustomerType(CustomerType.valueOf(csvrecord.get("customerType")));
                final String loyalty = csvrecord.get("loyaltyDiscountPercent");
                if (loyalty != null && !loyalty.isBlank()) {
                    customer.setLoyaltyDiscountPercent(BigDecimal.valueOf(Double.parseDouble(loyalty)));
                }
                customerRepository.save(customer);

            }
            //final List<String> result=Files.readAllLines(Path.of("src/main/resources/beispiel-kunden.csv"));
//            for (String element:result.subList(1,result.size())) {
//                final String[] data=element.trim().split(",");
//                final Customer customer = new Customer();
//
//                customer.setCustomerId(data[0]);
//                customer.setCustomerType(CustomerType.valueOf(data[1]));
//                customer.setLoyaltyDiscountPercent(BigDecimal.valueOf( Double.parseDouble(data[2])));
//                final Customer savedCustomer =customerRepository.save(customer);
//                System.out.println("result = " + savedCustomer);
//            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}





