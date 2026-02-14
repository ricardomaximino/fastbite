package es.brasatech.fastbite.mongodb.payment;

import es.brasatech.fastbite.application.payment.PaymentService;
import es.brasatech.fastbite.domain.payment.PaymentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class PaymentServiceMongoImpl implements PaymentService {
    private final PaymentConfigMongoRepository repository;

    @Override
    public PaymentConfig getConfig() {
        return repository.findById(PaymentConfig.DEFAULT_ID)
                .map(this::toDomain)
                .orElseGet(() -> new PaymentConfig(new ArrayList<>(), new ArrayList<>()));
    }

    @Override
    public void updateConfig(PaymentConfig config) {
        PaymentConfigDocument document = repository.findById(PaymentConfig.DEFAULT_ID)
                .orElse(new PaymentConfigDocument());
        document.setId(PaymentConfig.DEFAULT_ID);
        document.setActiveModes(config.activeModes());
        document.setMoneyImages(config.moneyImages());
        repository.save(document);
    }

    private PaymentConfig toDomain(PaymentConfigDocument document) {
        return new PaymentConfig(document.getId(), document.getActiveModes(), document.getMoneyImages());
    }
}
