package es.brasatech.fastbite.jpa.payment;

import es.brasatech.fastbite.domain.payment.MoneyType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;

@Embeddable
public class MoneyDenominationEmbeddable {
    @Column(name = "denomination_value")
    private BigDecimal value;
    private String image;
    @Enumerated(EnumType.STRING)
    private MoneyType type;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public MoneyType getType() {
        return type;
    }

    public void setType(MoneyType type) {
        this.type = type;
    }
}
