package es.brasatech.fastbite.mongodb.table;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.table.TableI18n;
import es.brasatech.fastbite.domain.table.TableStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class TableServiceMongoImpl implements TableService {
    private final TableMongoRepository repository;
    private final TableTranslationMongoRepository translationRepository;
    private final I18nConfig i18nConfig;
    private final es.brasatech.fastbite.mongodb.order.OrderMongoRepository orderRepository;

    @Override
    public List<Table> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Table> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Table create(Table table) {
        TableDocument document = new TableDocument();
        document.setId(table.id());
        document.setName(table.name());
        document.setSeats(table.seats());
        document.setStatus(table.status());
        document.setActive(table.active());
        return toDomain(repository.save(document));
    }

    @Override
    public Optional<Table> update(String id, Table table) {
        return repository.findById(id).map(document -> {
            document.setName(table.name());
            document.setSeats(table.seats());
            document.setStatus(table.status());
            document.setActive(table.active());
            return toDomain(repository.save(document));
        });
    }

    @Override
    public boolean delete(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<TableI18n> findI18nById(String id) {
        return repository.findById(id).map(document -> {
            List<TableTranslationDocument> translations = translationRepository.findAllByTableId(id);

            Map<String, String> nameMap = new HashMap<>();
            nameMap.put(i18nConfig.getDefaultLanguage(), document.getName());
            for (TableTranslationDocument translation : translations) {
                if (translation.getName() != null) {
                    nameMap.put(translation.getLanguage(), translation.getName());
                }
            }

            return new TableI18n(document.getId(), new I18nField(nameMap), document.getSeats(), document.getStatus(),
                    document.isActive());
        });
    }

    @Override
    public void updateI18n(String id, TableI18n i18n) {
        repository.findById(id).ifPresent(document -> {
            String defaultLang = i18nConfig.getDefaultLanguage();

            // Update main document with default language value
            document.setName(i18n.name().getDefault(defaultLang));
            document.setSeats(i18n.seats());
            document.setStatus(i18n.status());
            document.setActive(i18n.active());
            repository.save(document);

            // Update translations
            translationRepository.deleteByTableId(id);

            Map<String, String> nameTranslations = i18n.name().getAll();
            nameTranslations.forEach((lang, value) -> {
                if (!lang.equals(defaultLang) && value != null && !value.isEmpty()) {
                    translationRepository.save(new TableTranslationDocument(id, lang, value));
                }
            });
        });
    }

    @Override
    public void assignOrder(String tableId, String orderId) {
        repository.findById(tableId).ifPresent(document -> {
            if (!document.getOrderIds().contains(orderId)) {
                document.getOrderIds().add(orderId);
                document.setStatus(TableStatus.OCCUPIED);
                repository.save(document);
            }
        });
    }

    @Override
    public void unassignOrder(String tableId, String orderId) {
        repository.findById(tableId).ifPresent(document -> {
            document.getOrderIds().remove(orderId);
            if (document.getOrderIds().isEmpty()) {
                document.setStatus(TableStatus.AVAILABLE);
            }
            repository.save(document);
            resetTableSessionIfAllPaid(tableId);
        });
    }

    @Override
    public void resetTableSessionIfAllPaid(String tableId) {
        repository.findById(tableId).ifPresent(document -> {
            boolean allCleared = document.getOrderIds().stream()
                    .map(orderId -> orderRepository.findById(orderId))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .allMatch(order -> {
                        boolean isPaid = order
                                .getPaymentStatus() == es.brasatech.fastbite.domain.order.OrderPaymentStatus.PAID;
                        boolean isCancelled = order
                                .getStatus() == es.brasatech.fastbite.domain.order.OrderStatus.CANCELLED;
                        return isPaid || isCancelled;
                    });

            if (allCleared && !document.getOrderIds().isEmpty()) {
                document.setOrderIds(new java.util.ArrayList<>());
                document.setStatus(TableStatus.AVAILABLE);
                repository.save(document);
            }
        });
    }

    @Override
    public Optional<Table> findTableByOrderId(String orderId) {
        return repository.findAll().stream()
                .filter(table -> table.getOrderIds().contains(orderId))
                .findFirst()
                .map(this::toDomain);
    }

    private Table toDomain(TableDocument document) {
        return new Table(
                document.getId(),
                document.getName(),
                document.getSeats(),
                document.getStatus(),
                document.isActive(),
                new java.util.ArrayList<>(document.getOrderIds()));
    }
}
