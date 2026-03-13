package es.brasatech.fastbite.application.payment;

import es.brasatech.fastbite.domain.payment.PaymentConfig;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    PaymentConfig getActiveConfig();

    List<PaymentConfig> findAllConfigs();

    Optional<PaymentConfig> findConfigById(String id);

    void saveConfig(PaymentConfig config);

    void deleteConfig(String id);

    void setActiveConfig(String id);

    @Deprecated
    default PaymentConfig getConfig() {
        return getActiveConfig();
    }

    @Deprecated
    default void updateConfig(PaymentConfig config) {
        saveConfig(config);
    }
}
