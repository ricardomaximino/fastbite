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
    public PaymentConfig getActiveConfig() {
        return repository.findAll().stream()
                .filter(PaymentConfigDocument::isActive)
                .findFirst()
                .map(this::toDomain)
                .orElseGet(() -> {
                    PaymentConfig config = new PaymentConfig(new ArrayList<>(), new ArrayList<>());
                    saveConfig(config);
                    setActiveConfig(config.id());
                    return config;
                });
    }

    @Override
    public java.util.List<PaymentConfig> findAllConfigs() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.Optional<PaymentConfig> findConfigById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public void saveConfig(PaymentConfig config) {
        PaymentConfigDocument document = repository.findById(config.id())
                .orElse(new PaymentConfigDocument());
        document.setId(config.id());
        document.setActiveModes(config.activeModes());
        document.setMoneyDenominations(config.moneyDenominations());
        document.setActive(config.active());
        repository.save(document);
    }

    @Override
    public void deleteConfig(String id) {
        repository.deleteById(id);
    }

    @Override
    public void setActiveConfig(String id) {
        repository.findAll().forEach(doc -> {
            doc.setActive(doc.getId().equals(id));
            repository.save(doc);
        });
    }

    private PaymentConfig toDomain(PaymentConfigDocument document) {
        return new PaymentConfig(document.getId(), document.getActiveModes(), document.getMoneyDenominations(),
                document.isActive());
    }
}
