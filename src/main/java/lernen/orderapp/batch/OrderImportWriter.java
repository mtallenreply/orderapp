package lernen.orderapp.batch;

import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component

@RequiredArgsConstructor
public class OrderImportWriter implements ItemWriter<Order> {
    private final OrderRepository orderRepository;
    @Override
    public void write(Chunk<? extends Order> chunk) throws Exception {
        for (Order order : chunk) {
            orderRepository.save(order);
        }

    }
}
