package lernen.orderapp.batch;

import java.math.BigDecimal;

public record OrderImportZeile(String orderId,
                               String customerId,
                               String customerName,
                               String productSku,
                               Integer quantity,
                               BigDecimal unitPrice,
                               String orderDate,
                               String channel)
 {
}
