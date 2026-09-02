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
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsAggregatorService {
    private static final Function<Order, BigDecimal> earningsCalc
            = order -> order.getResultingPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

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

        final Comparator<Map.Entry<String, BigDecimal>> byEarningsDescending = Map.Entry.<String, BigDecimal>comparingByValue().reversed();
        // HilfsBedingung um nach Zeit zu filtern
        final Predicate<Order> filterTime = order ->
                !order.getOrderDate().before(Date.valueOf(from))
                        && !order.getOrderDate().after(Date.valueOf(to));

        // Hilfsfunktion um Customer Name als Bezeichnung in die Liste zu bekommen
        final Function<Map.Entry<String, BigDecimal>, TopCustomer> mapToTopCustomer = entry -> {
            final String customerName = customerRepository.findById(entry.getKey())
                    .flatMap(c -> Optional.ofNullable(c.getCustomerName()))
                    .orElse(entry.getKey());
            return new TopCustomer(entry.getKey(), customerName, entry.getValue());
        };

        //1. Hole alle Orders
        final List<Order> orderList = orderRepository.findAll();
        //2. Filtere alle Daten nach Von bis
        final List<Order> filteredOrders = orderList.stream()
                .filter(filterTime)
                .toList();

        //3. Groupiere und summiere nach Customer ID
        final Map<String, BigDecimal> earningsByCustomerId = filteredOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getCustomer().getId(),
                        Collectors.reducing(BigDecimal.ZERO, earningsCalc, BigDecimal::add)));
        // sorted() muss alle Kunden durchlaufen (stateful) - limit() kürzt erst danach.
        // Der DB-Aufruf in mapToTopCustomer läuft aber nur noch für die verbleibenden `limit` Kunden.
        return earningsByCustomerId.entrySet().stream()
                .sorted(byEarningsDescending)
                .limit(limit)
                .map(mapToTopCustomer)
                .toList();

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
