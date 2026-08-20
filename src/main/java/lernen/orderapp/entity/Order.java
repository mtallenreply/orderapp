package lernen.orderapp.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    private String id;
    @Column(nullable = false)
    private String productSku;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private BigDecimal unitPrice;
    @Column(nullable = false)
    private BigDecimal resultingPrice;
    @Column(nullable = false)
    private BigDecimal discountFactor;
    @Column(nullable = false)
    private Date orderDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Channel channel;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    private Customer customer;
}

