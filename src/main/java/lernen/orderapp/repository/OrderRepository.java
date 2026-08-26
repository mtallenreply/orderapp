package lernen.orderapp.repository;

import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
        SELECT o FROM Order o
        WHERE (:customerId IS NULL OR o.customer.id = :customerId)
          AND (:channel IS NULL OR o.channel = :channel)
          AND (:dateFrom IS NULL OR o.orderDate >= :dateFrom)
          AND (:dateTo IS NULL OR o.orderDate <= :dateTo)
        """,
            countQuery = """
        SELECT COUNT(o) FROM Order o
        WHERE (:customerId IS NULL OR o.customer.id = :customerId)
          AND (:channel IS NULL OR o.channel = :channel)
          AND (:dateFrom IS NULL OR o.orderDate >= :dateFrom)
          AND (:dateTo IS NULL OR o.orderDate <= :dateTo)
        """)
    Page<Order> findByFilters(@Param("customerId") String customerId,

                              @Param("dateFrom") Date dateFrom,
                              @Param("dateTo") Date dateTo,
                              @Param("channel") Channel channel,
                              Pageable pageable);

}
