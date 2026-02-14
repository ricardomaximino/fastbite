package es.brasatech.fastbite.jpa.order;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.product.ProductCustomizer;
import es.brasatech.fastbite.domain.product.ProductCustomizerI18n;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionEntity;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionJpaRepository;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionTranslationEntity;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionTranslationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of OrderService for relational databases.
 * Handles i18n by storing all product customizer names in default language
 * and translating on retrieval based on orderLanguage field.
 */
@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class OrderServiceJpaImpl implements OrderService {

    private final OrderJpaRepository repository;
    private final CustomizationOptionJpaRepository optionRepository;
    private final CustomizationOptionTranslationJpaRepository optionTranslationRepository;
    private final I18nConfig i18nConfig;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<Order> findAll() {
        return repository.findAll().stream()
                .map(this::toOrderWithTranslation)
                .toList();
    }

    @Override
    public Optional<Order> findById(String id) {
        return repository.findById(id)
                .map(this::toOrderWithTranslation);
    }

    @Override
    public Order create(Order order) {
        OrderEntity entity = toEntityWithDefaultLanguage(order, false);
        OrderEntity saved = repository.save(entity);
        return toOrderWithTranslation(saved);
    }

    @Override
    public Optional<Order> update(String id, Order order) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        OrderEntity entity = toEntityWithDefaultLanguage(order, true);
        entity.setId(id);
        OrderEntity saved = repository.save(entity);
        return Optional.of(toOrderWithTranslation(saved));
    }

    @Override
    public boolean delete(String id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Override
    public void clear() {
        repository.deleteAll();
    }

    @Override
    public Optional<ProductCustomizerI18n> findI18nById(String id) {
        return Optional.empty();
    }

    @Override
    public void publishEvent(Object event) {
        eventPublisher.publishEvent(event);
    }

    /**
     * Convert Order to OrderEntity, ensuring all ProductCustomizer names are in
     * default language.
     * If order language is not default, translate names to default language before
     * saving.
     *
     * @param order  The order to convert
     * @param setIds If true, sets IDs from order (for updates). If false, lets JPA
     *               generate IDs (for creates).
     */
    private OrderEntity toEntityWithDefaultLanguage(Order order, boolean setIds) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        String orderLang = order.orderLanguage();

        OrderEntity entity = new OrderEntity();
        // Only set ID for updates, not for creates (JPA will generate it)
        if (setIds && order.id() != null) {
            entity.setId(order.id());
        }
        entity.setOrderNumber(order.orderNumber());
        entity.setCreatedAt(order.createdAt());
        entity.setUpdatedAt(order.updatedAt());
        entity.setStatus(order.status());
        entity.setTotal(order.total());
        entity.setCancelReason(order.cancelReason());
        entity.setPaymentStatus(order.paymentStatus());
        entity.setOrderChannel(order.orderChannel());
        entity.setOrderLanguage(orderLang);
        entity.setTableId(order.tableId());
        entity.setUserId(order.userId());

        // Convert items
        if (order.items() != null) {
            List<CartItemEntity> itemEntities = new ArrayList<>();
            for (CartItem item : order.items()) {
                CartItemEntity itemEntity = new CartItemEntity();
                // Don't set CartItemEntity ID - let JPA generate it
                itemEntity.setItemId(item.itemId());
                itemEntity.setName(item.name());
                itemEntity.setDescription(item.description());
                itemEntity.setImage(item.image());
                itemEntity.setQuantity(item.quantity());
                itemEntity.setPrice(item.price());

                // Convert customizations
                if (item.customizations() != null) {
                    List<ProductCustomizerEntity> customizerEntities = new ArrayList<>();
                    for (ProductCustomizer customizer : item.customizations()) {
                        ProductCustomizerEntity customizerEntity = new ProductCustomizerEntity();
                        // Always set ProductCustomizerEntity ID (it's the option ID, not
                        // auto-generated)
                        customizerEntity.setId(customizer.id());
                        customizerEntity.setPrice(customizer.price());
                        customizerEntity.setQuantity(customizer.quantity());

                        // If order language is not default, get the default language name
                        if (!orderLang.equals(defaultLang)) {
                            String defaultName = getDefaultLanguageName(customizer.id());
                            customizerEntity.setName(defaultName);
                        } else {
                            customizerEntity.setName(customizer.name());
                        }

                        customizerEntities.add(customizerEntity);
                    }
                    itemEntity.setCustomizations(customizerEntities);
                }

                itemEntities.add(itemEntity);
            }
            entity.setItems(itemEntities);
        }

        return entity;
    }

    /**
     * Get the default language name for a customization option.
     */
    private String getDefaultLanguageName(String optionId) {
        return optionRepository.findById(optionId)
                .map(CustomizationOptionEntity::getName)
                .orElse("");
    }

    /**
     * Convert OrderEntity to Order, translating ProductCustomizer names if needed.
     */
    private Order toOrderWithTranslation(OrderEntity entity) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        String orderLang = entity.getOrderLanguage();

        List<CartItem> items = null;
        if (entity.getItems() != null) {
            items = new ArrayList<>();
            for (CartItemEntity itemEntity : entity.getItems()) {
                List<ProductCustomizer> customizers = null;
                if (itemEntity.getCustomizations() != null) {
                    customizers = new ArrayList<>();
                    for (ProductCustomizerEntity customizerEntity : itemEntity.getCustomizations()) {
                        String name;

                        // If order language is not default, translate the name
                        if (!orderLang.equals(defaultLang)) {
                            name = getTranslatedName(customizerEntity.getId(), orderLang, customizerEntity.getName());
                        } else {
                            name = customizerEntity.getName();
                        }

                        ProductCustomizer customizer = new ProductCustomizer(
                                customizerEntity.getId(),
                                name,
                                customizerEntity.getPrice(),
                                customizerEntity.getQuantity());
                        customizers.add(customizer);
                    }
                }

                CartItem item = new CartItem(
                        itemEntity.getId(),
                        itemEntity.getItemId(),
                        itemEntity.getName(),
                        itemEntity.getDescription(),
                        itemEntity.getImage(),
                        itemEntity.getQuantity(),
                        customizers,
                        itemEntity.getPrice());
                items.add(item);
            }
        }

        return new Order(
                items,
                entity.getOrderNumber(),
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getStatus(),
                entity.getTotal(),
                entity.getCancelReason(),
                entity.getPaymentStatus(),
                entity.getOrderChannel(),
                entity.getOrderLanguage(),
                entity.getTableId(),
                entity.getUserId());
    }

    /**
     * Get translated name for a customization option, with fallback to default
     * language.
     */
    private String getTranslatedName(String optionId, String language, String defaultName) {
        return optionTranslationRepository.findByOptionIdAndLanguage(optionId, language)
                .map(CustomizationOptionTranslationEntity::getName)
                .filter(name -> name != null && !name.isEmpty())
                .orElse(defaultName);
    }
}
