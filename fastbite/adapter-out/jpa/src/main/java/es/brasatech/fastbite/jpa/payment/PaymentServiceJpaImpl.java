package es.brasatech.fastbite.jpa.payment;

import es.brasatech.fastbite.application.payment.PaymentService;
import es.brasatech.fastbite.domain.payment.PaymentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class PaymentServiceJpaImpl implements PaymentService {
    private final PaymentConfigJpaRepository repository;

    @Override
    public PaymentConfig getConfig() {
        return repository.findById(PaymentConfig.DEFAULT_ID)
                .map(this::toDomain)
                .orElseGet(() -> new PaymentConfig(new ArrayList<>(), new ArrayList<>()));
    }

    @Override
    public void updateConfig(PaymentConfig config) {
        PaymentConfigEntity entity = repository.findById(PaymentConfig.DEFAULT_ID)
                .orElse(new PaymentConfigEntity());
        entity.setId(PaymentConfig.DEFAULT_ID);
        entity.setActiveModes(config.activeModes());
        entity.setMoneyImages(config.moneyImages());
        repository.save(entity);
    }

    private PaymentConfig toDomain(PaymentConfigEntity entity) {
        return new PaymentConfig(entity.getId(), entity.getActiveModes(), entity.getMoneyImages());
    }
}
