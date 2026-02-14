package es.brasatech.fastbite.mongodb.order;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.domain.customization.CustomizationOptionDto;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.product.ProductCustomizer;
import es.brasatech.fastbite.domain.product.ProductCustomizerI18n;
import es.brasatech.fastbite.mongodb.customization.CustomizationDocument;
import es.brasatech.fastbite.mongodb.customization.CustomizationMongoRepository;
import es.brasatech.fastbite.mongodb.customization.CustomizationOptionTranslationDocument;
import es.brasatech.fastbite.mongodb.customization.CustomizationOptionTranslationMongoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation of OrderService for NoSQL databases.
 * Handles i18n by storing all product customizer names in default language
 * and translating on retrieval based on orderLanguage field.
 */
@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class OrderServiceMongoImpl implements OrderService {

    private final OrderMongoRepository repository;
    private final CustomizationMongoRepository customizationRepository;
    private final CustomizationOptionTranslationMongoRepository optionTranslationRepository;
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
        OrderDocument document = toDocumentWithDefaultLanguage(order);
        OrderDocument saved = repository.save(document);
        return toOrderWithTranslation(saved);
    }

    @Override
    public Optional<Order> update(String id, Order order) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        OrderDocument document = toDocumentWithDefaultLanguage(order);
        document.setId(id);
        OrderDocument saved = repository.save(document);
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
     * Convert Order to OrderDocument, ensuring all ProductCustomizer names are in
     * default language.
     * If order language is not default, translate names to default language before
     * saving.
     */
    private OrderDocument toDocumentWithDefaultLanguage(Order order) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        String orderLang = order.orderLanguage();

        OrderDocument document = new OrderDocument();
        document.setId(order.id());
        document.setOrderNumber(order.orderNumber());
        document.setCreatedAt(order.createdAt());
        document.setUpdatedAt(order.updatedAt());
        document.setStatus(order.status());
        document.setTotal(order.total());
        document.setCancelReason(order.cancelReason());
        document.setPaymentStatus(order.paymentStatus());
        document.setOrderChannel(order.orderChannel());
        document.setOrderLanguage(orderLang);
        document.setTableId(order.tableId());
        document.setUserId(order.userId());

        // Convert items
        if (order.items() != null) {
            List<CartItemDocument> itemDocuments = new ArrayList<>();
            for (CartItem item : order.items()) {
                CartItemDocument itemDoc = new CartItemDocument();
                itemDoc.setId(item.id());
                itemDoc.setItemId(item.itemId());
                itemDoc.setName(item.name());
                itemDoc.setDescription(item.description());
                itemDoc.setImage(item.image());
                itemDoc.setQuantity(item.quantity());
                itemDoc.setPrice(item.price());

                // Convert customizations
                if (item.customizations() != null) {
                    List<ProductCustomizerDocument> customizerDocuments = new ArrayList<>();
                    for (ProductCustomizer customizer : item.customizations()) {
                        ProductCustomizerDocument customizerDoc = new ProductCustomizerDocument();
                        customizerDoc.setId(customizer.id());
                        customizerDoc.setPrice(customizer.price());
                        customizerDoc.setQuantity(customizer.quantity());

                        // If order language is not default, get the default language name
                        if (!orderLang.equals(defaultLang)) {
                            String defaultName = getDefaultLanguageName(customizer.id());
                            customizerDoc.setName(defaultName);
                        } else {
                            customizerDoc.setName(customizer.name());
                        }

                        customizerDocuments.add(customizerDoc);
                    }
                    itemDoc.setCustomizations(customizerDocuments);
                }

                itemDocuments.add(itemDoc);
            }
            document.setItems(itemDocuments);
        }

        return document;
    }

    /**
     * Get the default language name for a customization option.
     * Option ID format: "customizationId-opt-index"
     */
    private String getDefaultLanguageName(String optionId) {
        // Parse option ID to extract customization ID and option index
        int lastDash = optionId.lastIndexOf("-opt-");
        if (lastDash == -1) {
            return "";
        }

        String customizationId = optionId.substring(0, lastDash);
        String indexStr = optionId.substring(lastDash + 5);

        try {
            int optionIndex = Integer.parseInt(indexStr);

            return customizationRepository.findById(customizationId)
                    .map(CustomizationDocument::getOptions)
                    .filter(options -> optionIndex >= 0 && optionIndex < options.size())
                    .map(options -> options.get(optionIndex))
                    .map(CustomizationOptionDto::name)
                    .orElse("");
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * Convert OrderDocument to Order, translating ProductCustomizer names if
     * needed.
     */
    private Order toOrderWithTranslation(OrderDocument document) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        String orderLang = document.getOrderLanguage();

        List<CartItem> items = null;
        if (document.getItems() != null) {
            items = new ArrayList<>();
            for (CartItemDocument itemDoc : document.getItems()) {
                List<ProductCustomizer> customizers = null;
                if (itemDoc.getCustomizations() != null) {
                    customizers = new ArrayList<>();
                    for (ProductCustomizerDocument customizerDoc : itemDoc.getCustomizations()) {
                        String name;

                        // If order language is not default, translate the name
                        if (!orderLang.equals(defaultLang)) {
                            name = getTranslatedName(customizerDoc.getId(), orderLang, customizerDoc.getName());
                        } else {
                            name = customizerDoc.getName();
                        }

                        ProductCustomizer customizer = new ProductCustomizer(
                                customizerDoc.getId(),
                                name,
                                customizerDoc.getPrice(),
                                customizerDoc.getQuantity());
                        customizers.add(customizer);
                    }
                }

                CartItem item = new CartItem(
                        itemDoc.getId(),
                        itemDoc.getItemId(),
                        itemDoc.getName(),
                        itemDoc.getDescription(),
                        itemDoc.getImage(),
                        itemDoc.getQuantity(),
                        customizers,
                        itemDoc.getPrice());
                items.add(item);
            }
        }

        return new Order(
                items,
                document.getOrderNumber(),
                document.getId(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getStatus(),
                document.getTotal(),
                document.getCancelReason(),
                document.getPaymentStatus(),
                document.getOrderChannel(),
                document.getOrderLanguage(),
                document.getTableId(),
                document.getUserId());
    }

    /**
     * Get translated name for a customization option, with fallback to default
     * language.
     */
    private String getTranslatedName(String optionId, String language, String defaultName) {
        return optionTranslationRepository.findByOptionIdAndLanguage(optionId, language)
                .map(CustomizationOptionTranslationDocument::getName)
                .filter(name -> name != null && !name.isEmpty())
                .orElse(defaultName);
    }
}
