package lernen.orderapp.service;


import lernen.orderapp.entity.Channel;
import lernen.orderapp.repository.CustomerRepository;
import lernen.orderapp.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.Map;




class StatisticsAggregatorIntTest {

    private StatisticsAggregator statisticsAggregatorUnderTest;
    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    @BeforeEach
    void setUp() {
        statisticsAggregatorUnderTest = new StatisticsAggregator(this.customerRepository, this.orderRepository);
    }

    @Test
    void testCalcNumberOfOrdersPerChannel() {
        // Setup
        // Run the test
        final Map<Channel, Long> result = statisticsAggregatorUnderTest.calcNumberOfOrdersPerChannel();
        System.out.println("result = " + result);

    }
    @Test
    void testCalcTotalEarnings() {
        // Setup
        // Run the test
        final BigDecimal result = statisticsAggregatorUnderTest.calcTotalEarnings();
        System.out.println("result = " + result);}
    @Test
    void testCalcTop5() {
        // Setup
        // Run the test
        final var result = statisticsAggregatorUnderTest.calcTop(
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2026-12-31"),5L);
        System.out.println("result = " + result);}

}
