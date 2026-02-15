package es.brasatech.fastbite.jpa.payment;

import es.brasatech.fastbite.application.payment.PaymentService;
import es.brasatech.fastbite.domain.payment.MoneyDenomination;
import es.brasatech.fastbite.domain.payment.PaymentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        // Wrap in ArrayList to ensure mutability for Hibernate
        entity.setActiveModes(new ArrayList<>(config.activeModes()));

        List<MoneyDenominationEmbeddable> embeddables = config.moneyDenominations().stream()
                .map(this::toEmbeddable)
                .collect(Collectors.toCollection(ArrayList::new));
        entity.setMoneyDenominations(embeddables);

        repository.save(entity);
    }

    private PaymentConfig toDomain(PaymentConfigEntity entity) {
        List<MoneyDenomination> denominations = entity.getMoneyDenominations().stream()
                .map(emb -> new MoneyDenomination(emb.getValue(), emb.getImage(), emb.getType()))
                .toList();
        return new PaymentConfig(entity.getId(), entity.getActiveModes(), denominations);
    }

    private MoneyDenominationEmbeddable toEmbeddable(MoneyDenomination domain) {
        MoneyDenominationEmbeddable emb = new MoneyDenominationEmbeddable();
        emb.setValue(domain.value());
        emb.setImage(domain.image());
        emb.setType(domain.type());
        return emb;
    }
}
