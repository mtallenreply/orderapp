package lernen.orderapp.repository;

import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomer_Id(String id);

    @Query("SELECT COALESCE(SUM(o.resultingPrice * o.quantity), 0) FROM Order o WHERE o.customer.id = :customerId")
    BigDecimal sumEarningsByCustomerId(@Param("customerId") String customerId);

    List<Order> findByCustomer_IdAndOrderDateBetweenAndChannelOrderByOrderDateDesc( String id,  Date orderDateStart,  Date orderDateEnd,  Channel channel);

    List<Order> findByCustomer_IdAndOrderDateBetweenAndChannel(String id, Date orderDateStart, Date orderDateEnd, Channel channel);
}
