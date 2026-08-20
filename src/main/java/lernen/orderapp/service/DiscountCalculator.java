package lernen.orderapp.service;

import lernen.orderapp.batch.OrderImportZeile;
import lernen.orderapp.entity.Channel;
import lernen.orderapp.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntPredicate;
@RequiredArgsConstructor
@Service
public  class DiscountCalculator {

    private static final BigDecimal QUANTITY_DISCOUNT_10 = BigDecimal.valueOf(0.05);
    private static final BigDecimal QUANTITY_DISCOUNT_50 = BigDecimal.valueOf(0.10);
    private static final BigDecimal PARTNER_DISCOUNT = BigDecimal.valueOf(0.03);
    private static final BigDecimal MAX_DISCOUNT = BigDecimal.valueOf(0.20);

    private static final IntPredicate lowerDiscountBorder = qty -> qty >= 10;
    private static final IntPredicate upperDiscountBorder = qty -> qty >= 50;
    private static final Function<OrderImportZeile,BigDecimal> quantityDiscountCalc=(a)
            ->
            lowerDiscountBorder.test(a.quantity()) ? QUANTITY_DISCOUNT_50
                    : upperDiscountBorder.test(a.quantity()) ? QUANTITY_DISCOUNT_10
                    : BigDecimal.ZERO;

    private static final Function<OrderImportZeile,BigDecimal> channelDiscountCalc= (line)
            -> line.channel().equals(Channel.PARTNER.toString()) ? PARTNER_DISCOUNT : BigDecimal.ZERO;

    private static final Function<Customer,BigDecimal> loyaltyDiscountCalc= (customer)
            -> Optional.ofNullable(customer.getLoyaltyDiscountPercent()).orElse(BigDecimal.ZERO);

    public static BigDecimal calculateDiscount(OrderImportZeile orderImportZeile, Customer customer) {
        final BigDecimal quantityDiscount = quantityDiscountCalc.apply(orderImportZeile);
        final BigDecimal channelDiscount = channelDiscountCalc.apply(orderImportZeile);
        final BigDecimal loyaltyDicount = loyaltyDiscountCalc.apply(customer);
        final BigDecimal totalDiscount = quantityDiscount.add(channelDiscount).add(loyaltyDicount);
        return totalDiscount.min(MAX_DISCOUNT);
}
}
