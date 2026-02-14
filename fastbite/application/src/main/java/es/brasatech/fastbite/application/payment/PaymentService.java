package es.brasatech.fastbite.application.payment;

import es.brasatech.fastbite.domain.payment.PaymentConfig;

public interface PaymentService {
    PaymentConfig getConfig();

    void updateConfig(PaymentConfig config);
}
