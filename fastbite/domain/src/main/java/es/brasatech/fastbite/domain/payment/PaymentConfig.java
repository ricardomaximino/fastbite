package es.brasatech.fastbite.domain.payment;

import java.util.List;

public record PaymentConfig(String id, List<String> activeModes, List<MoneyDenomination> moneyDenominations) {
    public static final String DEFAULT_ID = "global_config";

    public PaymentConfig(List<String> activeModes, List<MoneyDenomination> moneyDenominations) {
        this(DEFAULT_ID, activeModes, moneyDenominations);
    }
}
