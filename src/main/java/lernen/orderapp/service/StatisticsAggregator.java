package lernen.orderapp.service;


import jakarta.transaction.Transactional;
import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.CustomerRepository;
import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsAggregator {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    private final Function<Order, BigDecimal> earningsCalc
            = order -> order.getResultingPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

    public Page<Order> getOrders(final String customerId, final Channel channel, final LocalDate dateFrom, final LocalDate dateTo, final Pageable pageable) {
        if (customerId != null) {
            customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException(customerId));
        }
        return orderRepository.findByFilters(
                customerId,
                dateFrom != null ? Date.valueOf(dateFrom) : null,
                dateTo != null ? Date.valueOf(dateTo) : null,
                channel,
                pageable);

    }

    public Map<String, Object> getStatisticsOfCustomer(final String customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        final Long numberOfOrdersCustomer = (long) orderRepository.findByCustomer_Id(customerId).size();
        final BigDecimal totalEarningsCustomer = orderRepository.sumEarningsByCustomerId(customerId);
        final Map<String, Object> result = new HashMap<>();
        //Aggregierte Kennzahlen für einen Kunden (Gesamtumsatz, Bestellanzahl)
        result.put("number of Orders", numberOfOrdersCustomer);
        result.put("Total Earnings", totalEarningsCustomer);
        return result;
    }

    @Transactional
    public List<TopCustomer> calcTop(final LocalDate from, final LocalDate to, final Long limit) {
        final List<Order> orderList = orderRepository.findAll();

        final Predicate<Order> filterTime = order -> !order.getOrderDate().before(Date.valueOf(from))
                && !order.getOrderDate().after(Date.valueOf(to));

        final List<Order> filteredOrders = orderList.stream()
                .filter(filterTime)
                .toList();

        final Map<String, BigDecimal> earningsByCustomerId = filteredOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getCustomer().getId(),
                        Collectors.reducing(BigDecimal.ZERO, earningsCalc, BigDecimal::add)));

        return earningsByCustomerId.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new TopCustomer(
                        entry.getKey(),
                        customerRepository.findById(entry.getKey())
                                .flatMap(c -> Optional.ofNullable(c.getCustomerName()))
                                .orElse(entry.getKey()),
                        entry.getValue()))
                .toList();
    }

    private List<TopCustomer> calcTopImperative(final LocalDate from, final LocalDate to, final Long limit) {
        final List<Order> orderList = orderRepository.findAll();

        final Map<String, BigDecimal> earningsByCustomerId = new HashMap<>();
        for (final Order order : orderList) {
            final boolean inRange = !order.getOrderDate().before(Date.valueOf(from))
                    && !order.getOrderDate().after(Date.valueOf(to));
            if (!inRange) {
                continue;
            }
            final String customerId = order.getCustomer().getId();
            earningsByCustomerId.merge(customerId, earningsCalc.apply(order), BigDecimal::add);
        }

        final List<Map.Entry<String, BigDecimal>> sortedEntries = new ArrayList<>(earningsByCustomerId.entrySet());
        sortedEntries.sort(Map.Entry.<String, BigDecimal>comparingByValue().reversed());

        final List<TopCustomer> result = new ArrayList<>();
        for (final Map.Entry<String, BigDecimal> entry : sortedEntries) {
            if (result.size() >= limit) {
                break;
            }
            final String customerName = customerRepository.findById(entry.getKey())
                    .flatMap(c -> Optional.ofNullable(c.getCustomerName()))
                    .orElse(entry.getKey());
            result.add(new TopCustomer(entry.getKey(), customerName, entry.getValue()));
        }
        return result;
    }

    public BigDecimal calcTotalEarnings() {
        final List<Order> orderList = orderRepository.findAll();
        return orderList.stream().map(earningsCalc).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Channel, Long> calcNumberOfOrdersPerChannel() {
        final List<Order> orderList = orderRepository.findAll();
        return orderList.stream()
                .collect(Collectors.groupingBy(Order::getChannel, Collectors.counting()));
    }

    public record TopCustomer(String customerId, String customerName, BigDecimal totalEarnings) {
    }
}
