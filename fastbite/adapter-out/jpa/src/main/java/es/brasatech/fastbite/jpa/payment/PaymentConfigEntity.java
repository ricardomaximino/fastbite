package es.brasatech.fastbite.jpa.payment;

import jakarta.persistence.*;
import java.util.List;

@Entity(name = "PaymentConfig")
@Table(name = "payment_configs")
public class PaymentConfigEntity {
    @Id
    private String id;

    @ElementCollection
    @CollectionTable(name = "payment_modes", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "mode")
    private List<String> activeModes;

    @ElementCollection
    @CollectionTable(name = "money_denominations", joinColumns = @JoinColumn(name = "config_id"))
    private List<MoneyDenominationEmbeddable> moneyDenominations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getActiveModes() {
        return activeModes;
    }

    public void setActiveModes(List<String> activeModes) {
        this.activeModes = activeModes;
    }

    public List<MoneyDenominationEmbeddable> getMoneyDenominations() {
        return moneyDenominations;
    }

    public void setMoneyDenominations(List<MoneyDenominationEmbeddable> moneyDenominations) {
        this.moneyDenominations = moneyDenominations;
    }
}
