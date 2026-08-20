package lernen.orderapp.service;

import lernen.orderapp.batch.OrderImportZeile;
import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Customer;
import lernen.orderapp.entity.CustomerType;
import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.CustomerRepository;
import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;


import static lernen.orderapp.service.DiscountCalculator.calculateDiscount;

@Service
@RequiredArgsConstructor
public class OrderImportService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public Order orderImport(OrderImportZeile orderImportZeile) throws Exception {

        // customer id prüfen wenn vorhanden dann dort zuordnen
        // wenn nicht dann anlegen

        final Customer customer = customerRepository.findById(orderImportZeile.customerId())
                .orElseGet(() -> {
                    Customer neu = new Customer();
                    neu.setId(orderImportZeile.customerId());
                    neu.setCustomerType(CustomerType.STANDARD);
                    neu.setLoyaltyDiscountPercent(BigDecimal.ZERO);
                    neu.setCustomerName(orderImportZeile.customerName());
                    return customerRepository.save(neu);
                });
        if(!orderImportZeile.customerName().equals(customer.getCustomerName())) {
            customer.setCustomerName(orderImportZeile.customerName());
            customerRepository.save(customer);
        }
        final Order newOrder=new Order();
        newOrder.setId(orderImportZeile.orderId());
        newOrder.setOrderDate(Date.valueOf(orderImportZeile.orderDate()));
        newOrder.setProductSku(orderImportZeile.productSku());
        newOrder.setCustomer(customer);
        newOrder.setQuantity(orderImportZeile.quantity());
        newOrder.setUnitPrice(orderImportZeile.unitPrice());
        newOrder.setChannel(Channel.valueOf(orderImportZeile.channel()));
            //4.2
        final BigDecimal discount =calculateDiscount(orderImportZeile, customer);
        //4.3 zum teil
        newOrder.setDiscountFactor(discount);
        newOrder.setResultingPrice(orderImportZeile.unitPrice().multiply(BigDecimal.ONE.subtract(discount)));
        return newOrder;
        }

    }






