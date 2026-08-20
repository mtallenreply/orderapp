package lernen.orderapp.controller;

import lernen.orderapp.entity.Customer;

import java.math.BigDecimal;

public class DTO {
    public record CustomerResponse(
            String customerId,
            String customerType,
            BigDecimal loyaltyDiscountPercent

    ) {
        public static CustomerResponse from(Customer customer) {
            return new CustomerResponse(
                    customer.getId(),
                    customer.getCustomerType().toString(),
                    customer.getLoyaltyDiscountPercent());
        }
    }



    public record FehlerResponse(
            String zeitpunkt,
            Integer status,
            String  meldung
    ) {}

}