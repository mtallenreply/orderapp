package lernen.orderapp.service;

import lernen.orderapp.batch.OrderImportZeile;
import lernen.orderapp.entity.Customer;
import lernen.orderapp.entity.CustomerType;
import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.CustomerRepository;
import lernen.orderapp.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderImportServiceTest {

    @Mock
    private OrderRepository mockOrderRepository;
    @Mock
    private CustomerRepository mockCustomerRepository;

    private OrderImportService orderImportServiceUnderTest;

    @BeforeEach
    void setUp() {
        orderImportServiceUnderTest = new OrderImportService(mockOrderRepository, mockCustomerRepository);
    }

    @Test
    void testOrderImport() throws Exception {
        // Setup
        final OrderImportZeile orderImportZeile = new OrderImportZeile("orderId", "customerId", "customerName",
                "productSku", 0, new BigDecimal("0.00"), "orderDate", "channel");

        // Configure CustomerRepository.findById(...).
        final Customer customer1 = new Customer();
        customer1.setCustomerId("customerId");
        customer1.setCustomerName("customerName");
        customer1.setCustomerType(CustomerType.STANDARD);
        customer1.setLoyaltyDiscountPercent(new BigDecimal("0.00"));
        final Order order = new Order();
        customer1.setOrders(List.of(order));
        final Optional<Customer> customer = Optional.of(customer1);
        when(mockCustomerRepository.findById("customerId")).thenReturn(customer);

        // Run the test
        final Order result = orderImportServiceUnderTest.orderImport(orderImportZeile);

        // Verify the results
    }

    @Test
    void testOrderImport_CustomerRepositoryFindByIdReturnsAbsent() throws Exception {
        // Setup
        final OrderImportZeile orderImportZeile = new OrderImportZeile("orderId", "customerId", "customerName",
                "productSku", 0, new BigDecimal("0.00"), "orderDate", "channel");
        when(mockCustomerRepository.findById("customerId")).thenReturn(Optional.empty());

        // Configure CustomerRepository.save(...).
        final Customer customer = new Customer();
        customer.setCustomerId("customerId");
        customer.setCustomerName("customerName");
        customer.setCustomerType(CustomerType.STANDARD);
        customer.setLoyaltyDiscountPercent(new BigDecimal("0.00"));
        final Order order = new Order();
        customer.setOrders(List.of(order));
        when(mockCustomerRepository.save(any(Customer.class))).thenReturn(customer);

        // Run the test
        final Order result = orderImportServiceUnderTest.orderImport(orderImportZeile);

        // Verify the results
    }

    @Test
    void testOrderImport_CustomerRepositorySaveThrowsOptimisticLockingFailureException() {
        // Setup
        final OrderImportZeile orderImportZeile = new OrderImportZeile("orderId", "customerId", "customerName",
                "productSku", 0, new BigDecimal("0.00"), "orderDate", "channel");
        when(mockCustomerRepository.findById("customerId")).thenReturn(Optional.empty());
        when(mockCustomerRepository.save(any(Customer.class))).thenThrow(OptimisticLockingFailureException.class);

        // Run the test
        assertThatThrownBy(() -> orderImportServiceUnderTest.orderImport(orderImportZeile))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
