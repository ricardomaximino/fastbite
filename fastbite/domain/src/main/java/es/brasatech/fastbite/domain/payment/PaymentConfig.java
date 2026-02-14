package es.brasatech.fastbite.domain.payment;

import java.util.List;

public record PaymentConfig(String id, List<String> activeModes, List<String> moneyImages) {
    public static final String DEFAULT_ID = "global_config";

    public PaymentConfig(List<String> activeModes, List<String> moneyImages) {
        this(DEFAULT_ID, activeModes, moneyImages);
    }
}
