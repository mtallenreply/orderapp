package lernen.orderapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public  class Customer {
    @Id
    private String id;
    //das wird von den Beispieldaten nicht befüllt
    //Anzeigename des Kunden->  nullable da ich nicht von anfang an weiß was für ein Name der Kunde hat
    @Column()
    private String customerName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType;
    @Column( updatable = false)
    @Setter(AccessLevel.NONE)
    private BigDecimal loyaltyDiscountPercent;    //Individueller Treuerabatt in Prozent – nicht bei jedem Kunden gesetzt
    @OneToMany(mappedBy= "customer")
    private List<Order> orders;

    public Customer(final String id, final CustomerType customerType, final BigDecimal loyaltyDiscountPercent) {
        this.id = id;
        this.customerType = customerType;
        this.loyaltyDiscountPercent = loyaltyDiscountPercent;
    }
}

