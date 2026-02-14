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
    @CollectionTable(name = "payment_money_images", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "image_path")
    private List<String> moneyImages;

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

    public List<String> getMoneyImages() {
        return moneyImages;
    }

    public void setMoneyImages(List<String> moneyImages) {
        this.moneyImages = moneyImages;
    }
}
