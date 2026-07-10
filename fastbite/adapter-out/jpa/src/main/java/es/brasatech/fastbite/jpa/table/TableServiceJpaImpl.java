package es.brasatech.fastbite.jpa.table;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.table.TableI18n;
import es.brasatech.fastbite.domain.table.TableStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class TableServiceJpaImpl implements TableService {
    private final TableJpaRepository repository;
    private final TableTranslationJpaRepository translationRepository;
    private final I18nConfig i18nConfig;
    private final es.brasatech.fastbite.jpa.order.OrderJpaRepository orderRepository;
    private final es.brasatech.fastbite.application.table.TableSignatureUtil tableSignatureUtil;

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
        TableEntity entity = new TableEntity();
        if (table.id() != null && !table.id().isEmpty()) {
            entity.setId(table.id());
        }
        entity.setName(table.name());
        entity.setSeats(table.seats());
        entity.setStatus(table.status());
        entity.setActive(table.active());
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Table> update(String id, Table table) {
        return repository.findById(id).map(entity -> {
            entity.setName(table.name());
            entity.setSeats(table.seats());
            entity.setStatus(table.status());
            entity.setActive(table.active());
            return toDomain(entity);
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
        return repository.findById(id).map(entity -> {
            List<TableTranslationEntity> translations = translationRepository.findAllByTableId(id);

            Map<String, String> nameMap = new HashMap<>();
            nameMap.put(i18nConfig.getDefaultLanguage(), entity.getName());
            for (TableTranslationEntity translation : translations) {
                if (translation.getName() != null) {
                    nameMap.put(translation.getLanguage(), translation.getName());
                }
            }

            return new TableI18n(entity.getId(), new I18nField(nameMap), entity.getSeats(), entity.getStatus(),
                    entity.isActive());
        });
    }

    @Override
    public void updateI18n(String id, TableI18n i18n) {
        repository.findById(id).ifPresent(entity -> {
            String defaultLang = i18nConfig.getDefaultLanguage();

            // Update main entity with default language value
            entity.setName(i18n.name().getDefault(defaultLang));
            entity.setSeats(i18n.seats());
            entity.setStatus(i18n.status());
            entity.setActive(i18n.active());

            // Update translations
            translationRepository.deleteByTableId(id);

            Map<String, String> nameTranslations = i18n.name().getAll();
            nameTranslations.forEach((lang, value) -> {
                if (!lang.equals(defaultLang) && value != null && !value.isEmpty()) {
                    translationRepository.save(new TableTranslationEntity(entity, lang, value));
                }
            });
        });
    }

    @Override
    public void assignOrder(String tableId, String orderId) {
        repository.findById(tableId).ifPresent(entity -> {
            if (!entity.getOrderIds().contains(orderId)) {
                entity.getOrderIds().add(orderId);
                entity.setStatus(TableStatus.OCCUPIED);
            }
        });
    }

    @Override
    public void unassignOrder(String tableId, String orderId) {
        repository.findById(tableId).ifPresent(entity -> {
            entity.getOrderIds().remove(orderId);
            if (entity.getOrderIds().isEmpty()) {
                entity.setStatus(TableStatus.AVAILABLE);
            }
            resetTableSessionIfAllPaid(tableId);
        });
    }

    @Override
    public void resetTableSessionIfAllPaid(String tableId) {
        repository.findById(tableId).ifPresent(entity -> {
            boolean allCleared = entity.getOrderIds().stream()
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

            if (allCleared && !entity.getOrderIds().isEmpty()) {
                entity.setOrderIds(new java.util.ArrayList<>());
                entity.setStatus(TableStatus.AVAILABLE);
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

    @Override
    public Optional<Table> findTableByNameOrId(String tableParam) {
        if (tableParam == null || tableParam.trim().isEmpty()) {
            return Optional.empty();
        }
        var opt = findById(tableParam);
        if (opt.isPresent()) {
            return opt;
        }
        var tables = findAll();
        for (var t : tables) {
            if (t.name().equalsIgnoreCase(tableParam) || t.name().equalsIgnoreCase("Table " + tableParam) || t.id().equals(tableParam)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    @Override
    public String validateAndBindTableSession(String tableParam, String tokenParam, String existingTableNumber, List<?> cartItems) {
        if (tableParam == null || tableParam.trim().isEmpty()) {
            return null;
        }
        var opt = findTableByNameOrId(tableParam);
        if (opt.isPresent()) {
            var foundTable = opt.get();
            // Cryptographic validation of signed URL parameter
            if (tokenParam == null || !tableSignatureUtil.isValid(foundTable.id(), tokenParam)) {
                throw new IllegalArgumentException("Invalid or missing secure table token. Scan the QR code at your table.");
            }

            // Session boundary protection: Active unpaid order on Table A cannot switch to Table B
            if (existingTableNumber != null && !existingTableNumber.equals(foundTable.id())) {
                if (cartItems != null && !cartItems.isEmpty()) {
                    throw new IllegalStateException("You have an active ordering session on another table. Please complete or clear it first.");
                }
            }
            return foundTable.id();
        }
        return null;
    }

    private Table toDomain(TableEntity entity) {
        return new Table(
                entity.getId(),
                entity.getName(),
                entity.getSeats(),
                entity.getStatus(),
                entity.isActive(),
                new java.util.ArrayList<>(entity.getOrderIds()));
    }
}
