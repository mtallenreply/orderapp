package lernen.orderapp.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Order;


import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;


public final class DTO {
    public record OrderRequest(
            @Nullable @Schema(description = "ID des Kunden, nach dem gefiltert wird",
                    example = "C-1001") String customerId,
            @Nullable  @Schema(description = "Vertriebskanal, nach dem gefiltert wird") Channel channel,
            @Schema(description = "Start des Auswertungszeitraums (Format: yyyy-MM-dd)",
                    pattern = "\\d{4}-\\d{2}-\\d{2}",
                    example = "2026-01-01") LocalDate dateFrom,
            @Schema(description = "Ende des Auswertungszeitraums (Format: yyyy-MM-dd)",
                    pattern = "\\d{4}-\\d{2}-\\d{2}",
                    example = "2026-08-25") LocalDate dateTo
    ){
    }
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
        public static OrderResponse from(final Order order) {
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