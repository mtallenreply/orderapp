package lernen.orderapp.config;

import lernen.orderapp.entity.Customer;
import lernen.orderapp.entity.CustomerType;
import lernen.orderapp.repository.CustomerRepository;
//import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Order(0)
@Component
@RequiredArgsConstructor
public class SeedDataLoader implements CommandLineRunner {
    private final CustomerRepository customerRepository;
    //private final OrderRepository orderRepository;



    @Override
    public void run(String... args)  {


        //Musterbank Münster, BIC MUSTDE55XXX
        try (Reader in = Files.newBufferedReader(Path.of("src/main/resources/beispiel-kunden.csv"));){
                 final CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(in);

            for (CSVRecord record : parser) {
                final Customer customer = new Customer();
                customer.setId(record.get("customerId"));
                customer.setCustomerType(CustomerType.valueOf(record.get("customerType")));
                final String loyalty = record.get("loyaltyDiscountPercent");
                if (loyalty != null && !loyalty.isBlank()) {
                    customer.setLoyaltyDiscountPercent(BigDecimal.valueOf(Double.parseDouble(loyalty)));
                }
                final Customer savedCustomer =customerRepository.save(customer);
                System.out.println("result = " + savedCustomer);

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





