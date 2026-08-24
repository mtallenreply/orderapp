
package lernen.orderapp.batch;

import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Customer;
import lernen.orderapp.entity.CustomerType;
import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.CustomerRepository;
import lernen.orderapp.service.DiscountCalculator;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;

@Component
@RequiredArgsConstructor
public class OrderImportInputProcessor implements ItemProcessor<OrderImportZeile, Order> {

    private final CustomerRepository customerRepository;

    @Override
    public @Nullable Order process(final OrderImportZeile orderImportZeile) {
        Customer customer;
        try {
            customer = customerRepository.findById(orderImportZeile.customerId()).orElse(null);
            if (customer == null) {
                final Customer neu = new Customer();
                neu.setId(orderImportZeile.customerId());
                neu.setCustomerType(CustomerType.STANDARD);
                neu.setLoyaltyDiscountPercent(BigDecimal.ZERO);
                neu.setCustomerName(orderImportZeile.customerName());
                customer = customerRepository.save(neu);
            }
        } catch (final DataIntegrityViolationException e) {
            // ein anderer Thread war schneller und hat den Kunden bereits angelegt
            customer = customerRepository.findById(orderImportZeile.customerId())
                    .orElseThrow(() -> e);
        }
        if (!orderImportZeile.customerName().equals(customer.getCustomerName())) {
            customer.setCustomerName(orderImportZeile.customerName());
            customerRepository.save(customer);
        }

        final Order newOrder = new Order();
        newOrder.setId(orderImportZeile.orderId());
        newOrder.setOrderDate(Date.valueOf(orderImportZeile.orderDate()));
        newOrder.setProductSku(orderImportZeile.productSku());
        newOrder.setCustomer(customer);
        newOrder.setQuantity(orderImportZeile.quantity());
        newOrder.setUnitPrice(orderImportZeile.unitPrice());
        newOrder.setChannel(Channel.valueOf(orderImportZeile.channel()));
        final BigDecimal discount = DiscountCalculator.calculateDiscount(orderImportZeile, customer);
        newOrder.setDiscountFactor(discount);
        newOrder.setResultingPrice(orderImportZeile.unitPrice().multiply(BigDecimal.ONE.subtract(discount)));
        return newOrder;
    }
}