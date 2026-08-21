package lernen.orderapp.service;


import lernen.orderapp.entity.Channel;
import lernen.orderapp.repository.CustomerRepository;
import lernen.orderapp.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StatisticsAggregatorIntTest {


    @Autowired
    private StatisticsAggregator statisticsAggregatorUnderTest;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testCalcNumberOfOrdersPerChannel() {
        final Map<Channel, Long> result = statisticsAggregatorUnderTest.calcNumberOfOrdersPerChannel();
        System.out.println("result = " + result);

        assertThat(result).isEqualTo(Map.of(
                Channel.PARTNER, 7L,
                Channel.ONLINE, 9L,
                Channel.RETAIL, 8L
        ));

    }
    @Test
    void testCalcTotalEarnings() {
        final BigDecimal result = statisticsAggregatorUnderTest.calcTotalEarnings();
        System.out.println("result = " + result);
        assertThat(result).isEqualTo(new BigDecimal("4693.52"));

        System.out.println("result = " + result);}
    @Test
    void testCalcTop() {
        final List<String> result = statisticsAggregatorUnderTest.calcTop(
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2026-12-31"),5L);
        System.out.println("result = " + result);
        // Erst der erste Lauf des ImportJobs verbindet namen mit nummern
//        assertThat(result).containsExactly("C-1003", "C-1002", "C-1001", "C-1005", "C-1004");
        assertThat(result).containsExactly("Clara Voss", "Bernd Klein", "Anna Berger", "Erika Sommer", "Dieter Wolf");
    }

}
