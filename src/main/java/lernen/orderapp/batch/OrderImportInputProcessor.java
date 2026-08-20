package lernen.orderapp.batch;

import lernen.orderapp.entity.Order;
import lernen.orderapp.service.OrderImportService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component

@RequiredArgsConstructor
public class OrderImportInputProcessor implements ItemProcessor<OrderImportZeile,Order> {

    private final OrderImportService orderImportService;
    @Override
    public @Nullable Order process(OrderImportZeile orderImportZeile) throws Exception {

        return orderImportService.orderImport(orderImportZeile);
    }
}
