package es.brasatech.fastbite.mongodb.payment;

import es.brasatech.fastbite.domain.payment.MoneyDenomination;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "payment_configs")
public class PaymentConfigDocument {
    @Id
    private String id;
    private List<String> activeModes;
    private List<MoneyDenomination> moneyDenominations;
    private Boolean active = false;

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

    public List<MoneyDenomination> getMoneyDenominations() {
        return moneyDenominations;
    }

    public void setMoneyDenominations(List<MoneyDenomination> moneyDenominations) {
        this.moneyDenominations = moneyDenominations;
    }

    public Boolean isActive() {
        return active != null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
