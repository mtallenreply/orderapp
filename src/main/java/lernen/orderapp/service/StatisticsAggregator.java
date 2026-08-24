package lernen.orderapp.service;


import jakarta.transaction.Transactional;
import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.CustomerRepository;
import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
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

    private final Function<Order,BigDecimal> earningsCalc
            =order -> order.getResultingPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

    public List<Order> getOrders(final String customerId,final  Channel channel,final  LocalDate dateFrom, final LocalDate dateTo,final  boolean sorting){
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        if (sorting){
            return orderRepository.findByCustomer_IdAndOrderDateBetweenAndChannelOrderByOrderDateDesc(
                    customerId,
                     Date.valueOf(dateFrom),
                    Date.valueOf(dateTo),
                    channel
                     );
        } else {

            return orderRepository.findByCustomer_IdAndOrderDateBetweenAndChannel(    
                    customerId,
                    Date.valueOf(dateFrom),
                    Date.valueOf(dateTo),
                    channel
            );
        }
        
       
    }

    public Map<String,Object> getStatisticsOfCustomer(final  String customerId){
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        final Long numberOfOrdersCustomer = (long) orderRepository.findByCustomer_Id(customerId).size();
        final BigDecimal totalEarningsCustomer = orderRepository.sumEarningsByCustomerId(customerId);
        final Map<String, Object> result=new HashMap<>();
        //Aggregierte Kennzahlen für einen Kunden (Gesamtumsatz, Bestellanzahl)
        result.put("number of Orders",numberOfOrdersCustomer);
        result.put("Total Earnings",totalEarningsCustomer);
        return result;
    }
  
    @Transactional
    public List<String> calcTop(final LocalDate from, final LocalDate to, final Long limit){
        final List<Order> orderList=orderRepository.findAll();


        final Predicate<Order> filterTime=(a)-> a.getOrderDate().after(Date.valueOf(from)) &&
                a.getOrderDate().before(Date.valueOf(to));
        final Comparator<Map.Entry<String, BigDecimal>>test=Map.Entry.comparingByValue();

        return orderList.stream()
                .filter(filterTime)
                .collect(
                        Collectors.groupingBy(order -> Optional.ofNullable(order.getCustomer().getCustomerName())
                                        .orElse(order.getCustomer().getId()),
                        Collectors.reducing(BigDecimal.ZERO, earningsCalc,
                                BigDecimal::add)))

                .entrySet().stream()
                .sorted(test.reversed())
                //.peek((a)-> System.out.println(a))
                .map(Map.Entry::getKey)
                //.peek((a)-> System.out.println(a))
                .limit(limit)
                .toList();
        }


        public BigDecimal calcTotalEarnings() {
            final List<Order> orderList=orderRepository.findAll();
            return orderList.stream().map(earningsCalc).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public Map<Channel, Long> calcNumberOfOrdersPerChannel(){
            final List<Order> orderList=orderRepository.findAll();
            return orderList.stream()
                    .collect(Collectors.groupingBy(Order::getChannel, Collectors.counting()));
            }
}
