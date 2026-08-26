package lernen.orderapp.service;

import lernen.orderapp.batch.OrderImportZeile;
import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Customer;
import lernen.orderapp.entity.CustomerType;
import lernen.orderapp.entity.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountCalculatorTest {

    @Test
    void testCalculateDiscount() {
        // Setup
        final OrderImportZeile orderImportZeile = new OrderImportZeile("orderId",
                "customerId",
                "customerName",
                "productSku",
                100,
                new BigDecimal("10.00"),
                "orderDate", "channel");
        final Customer customer = new Customer("customerId", CustomerType.PREMIUM, new BigDecimal("15"));
        customer.setCustomerName("customerName");
        final Order order = new Order();
        customer.setOrders(List.of(order));

        // Run the test
        final BigDecimal result = DiscountCalculator.calculateDiscount(orderImportZeile, customer);

        // Verify the results
        assertThat(result).isEqualTo(new BigDecimal("0.2"));
    }
    @Test
    void testCalculateDiscount2() {
        // Setup
        final OrderImportZeile orderImportZeile = new OrderImportZeile("orderId",
                "customerId",
                "customerName",
                "productSku",
                10,
                new BigDecimal("10.0"),
                "orderDate", "channel");
        final Customer customer = new Customer("customerId", CustomerType.PREMIUM, new BigDecimal("15"));
        customer.setCustomerName("customerName");
        final Order order = new Order();
        customer.setOrders(List.of(order));

        // Run the test
        final BigDecimal result = DiscountCalculator.calculateDiscount(orderImportZeile, customer);

        // Verify the results
        assertThat(result).isEqualTo(new BigDecimal("0.2000"));
    }
    @Test
    void testCalculateDiscount3() {
        // Setup
        final OrderImportZeile orderImportZeile = new OrderImportZeile("orderId",
                "customerId",
                "customerName",
                "productSku",
                5,
                new BigDecimal("10.0"),
                "orderDate", Channel.PARTNER.toString());
        final Customer customer = new Customer("customerId", CustomerType.PREMIUM, new BigDecimal("15"));
        customer.setCustomerName("customerName");
        final Order order = new Order();
        customer.setOrders(List.of(order));

        // Run the test
        final BigDecimal result = DiscountCalculator.calculateDiscount(orderImportZeile, customer);

        // Verify the results
        assertThat(result).isEqualTo(new BigDecimal("0.1800"));
    }
}
