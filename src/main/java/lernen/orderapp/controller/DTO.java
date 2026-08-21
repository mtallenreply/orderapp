package lernen.orderapp.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Order;


import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

public class DTO {

    public record OrderRequest(
            String customerId,
            Channel channel,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean sorting
    ){}
    @Schema(description = "Antwortobjekt für eine einzelne Bestellung")
    public record OrderResponse(
            @Schema(description = "Bestell-ID") String orderId,
            @Schema(description = "Artikelnummer des Produkts") String productSku,
            @Schema(description = "Bestellte Menge") Integer quantity,
            @Schema(description = "Ursprünglicher Stückpreis netto") BigDecimal unitPrice,
            @Schema(description = "Preis nach Rabatt") BigDecimal resultingPrice,
            @Schema(description = "Angewendeter Rabattfaktor") BigDecimal discountFactor,
            @Schema(description = "Bestelldatum") Date orderDate,
            @Schema(description = "Vertriebskanal: ONLINE, RETAIL, PARTNER") String channel
    ){
        public static OrderResponse from(Order order) {
            return new OrderResponse(
                    order.getId(),
                    order.getProductSku(),
                    order.getQuantity(),
                    order.getUnitPrice(),
                    order.getResultingPrice(),
                    order.getDiscountFactor(),
                    order.getOrderDate(),
                    order.getChannel().toString()
            );
        }
    }
}