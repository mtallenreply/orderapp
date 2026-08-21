package lernen.orderapp.batch;

import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;
// Erster Versuch bevor ich gemerkt habe, dass es schon etwas gibt
@Component
@RequiredArgsConstructor
public class OrderImportWriter implements ItemWriter<Order> {
    private final OrderRepository orderRepository;
    @Override
    public void write(Chunk<? extends Order> chunk)   {
        for (Order order : chunk) {
            orderRepository.save(order);
        }

    }
}
