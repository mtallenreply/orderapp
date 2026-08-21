package lernen.orderapp.repository;

import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomer_Id(String id);

    List<Order> findByCustomer_IdAndOrderDateBetweenAndChannelOrderByOrderDateDesc( String id,  Date orderDateStart,  Date orderDateEnd,  Channel channel);

    List<Order> findByCustomer_IdAndOrderDateBetweenAndChannel(String id, Date orderDateStart, Date orderDateEnd, Channel channel);
}
