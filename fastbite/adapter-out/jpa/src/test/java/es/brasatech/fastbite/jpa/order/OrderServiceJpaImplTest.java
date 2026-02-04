package es.brasatech.fastbite.jpa.order;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.product.ProductCustomizer;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionEntity;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionJpaRepository;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionTranslationEntity;
import es.brasatech.fastbite.jpa.customization.CustomizationOptionTranslationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceJpaImplTest {

    @Mock
    private OrderJpaRepository repository;

    @Mock
    private CustomizationOptionJpaRepository optionRepository;

    @Mock
    private CustomizationOptionTranslationJpaRepository optionTranslationRepository;

    @Mock
    private I18nConfig i18nConfig;

    @InjectMocks
    private OrderServiceJpaImpl service;

    private static final String DEFAULT_LANG = "en";
    private static final String OPTION_ID = "cust-1-opt-0";

    @BeforeEach
    void setUp() {
        when(i18nConfig.getDefaultLanguage()).thenReturn(DEFAULT_LANG);
    }

    @Test
    void create_withDefaultLanguage_shouldSaveDirectly() {
        // Given
        ProductCustomizer customizer = new ProductCustomizer(OPTION_ID, "Cheese", BigDecimal.valueOf(1.5), 1);
        CartItem item = new CartItem("item1", "prod1", "Burger", "Delicious", "img.jpg",
                1, List.of(customizer), BigDecimal.TEN);
        Order order = new Order(List.of(item), 1, OrderPaymentStatus.PAID, OrderChannel.WAITER, DEFAULT_LANG);

        OrderEntity savedEntity = createOrderEntity(order);
        when(repository.save(any(OrderEntity.class))).thenReturn(savedEntity);

        // When
        Order result = service.create(order);

        // Then
        assertThat(result.orderLanguage()).isEqualTo(DEFAULT_LANG);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).customizations()).hasSize(1);
        assertThat(result.items().get(0).customizations().get(0).name()).isEqualTo("Cheese");

        verify(optionRepository, never()).findById(any());
        verify(optionTranslationRepository, never()).findByOptionIdAndLanguage(any(), any());
    }

    @Test
    void create_withNonDefaultLanguage_shouldTranslateToDefaultLanguage() {
        // Given - order comes in Spanish
        ProductCustomizer customizer = new ProductCustomizer(OPTION_ID, "Queso", BigDecimal.valueOf(1.5), 1);
        CartItem item = new CartItem("item1", "prod1", "Hamburguesa", "Deliciosa", "img.jpg",
                1, List.of(customizer), BigDecimal.TEN);
        Order order = new Order(List.of(item), 1, OrderPaymentStatus.PAID, OrderChannel.WAITER, "es");

        // Mock: option repository returns default language name
        CustomizationOptionEntity optionEntity = new CustomizationOptionEntity();
        optionEntity.setId(OPTION_ID);
        optionEntity.setName("Cheese");
        when(optionRepository.findById(OPTION_ID)).thenReturn(Optional.of(optionEntity));

        OrderEntity savedEntity = createOrderEntity(order);
        savedEntity.getItems().get(0).getCustomizations().get(0).setName("Cheese");
        when(repository.save(any(OrderEntity.class))).thenReturn(savedEntity);

        // Mock: translation repository returns Spanish translation
        CustomizationOptionTranslationEntity translation = new CustomizationOptionTranslationEntity();
        translation.setLanguage("es");
        translation.setName("Queso");
        when(optionTranslationRepository.findByOptionIdAndLanguage(OPTION_ID, "es"))
                .thenReturn(Optional.of(translation));

        // When
        Order result = service.create(order);

        // Then - verify default language name was used for storage
        verify(optionRepository).findById(OPTION_ID);

        // The result should have the Spanish translation
        assertThat(result.items().get(0).customizations().get(0).name()).isEqualTo("Queso");
    }

    @Test
    void findById_withDefaultLanguage_shouldReturnDirectly() {
        // Given
        Order order = new Order(
                List.of(new CartItem("item1", "prod1", "Burger", "Delicious", "img.jpg",
                        1, List.of(new ProductCustomizer(OPTION_ID, "Cheese", BigDecimal.valueOf(1.5), 1)),
                        BigDecimal.TEN)),
                1, OrderPaymentStatus.PAID, OrderChannel.WAITER, DEFAULT_LANG
        );
        OrderEntity entity = createOrderEntity(order);
        when(repository.findById("order-1")).thenReturn(Optional.of(entity));

        // When
        Optional<Order> result = service.findById("order-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().orderLanguage()).isEqualTo(DEFAULT_LANG);
        assertThat(result.get().items().get(0).customizations().get(0).name()).isEqualTo("Cheese");

        verify(optionTranslationRepository, never()).findByOptionIdAndLanguage(any(), any());
    }

    @Test
    void findById_withNonDefaultLanguage_shouldTranslateFromDefaultLanguage() {
        // Given - order stored in default language (English)
        Order order = new Order(
                List.of(new CartItem("item1", "prod1", "Burger", "Delicious", "img.jpg",
                        1, List.of(new ProductCustomizer(OPTION_ID, "Cheese", BigDecimal.valueOf(1.5), 1)),
                        BigDecimal.TEN)),
                1, OrderPaymentStatus.PAID, OrderChannel.WAITER, "es"
        );
        OrderEntity entity = createOrderEntity(order);
        when(repository.findById("order-1")).thenReturn(Optional.of(entity));

        // Mock: translation repository returns Spanish translation
        CustomizationOptionTranslationEntity translation = new CustomizationOptionTranslationEntity();
        translation.setLanguage("es");
        translation.setName("Queso");
        when(optionTranslationRepository.findByOptionIdAndLanguage(OPTION_ID, "es"))
                .thenReturn(Optional.of(translation));

        // When
        Optional<Order> result = service.findById("order-1");

        // Then - verify Spanish translation was applied
        assertThat(result).isPresent();
        assertThat(result.get().items().get(0).customizations().get(0).name()).isEqualTo("Queso");

        verify(optionTranslationRepository).findByOptionIdAndLanguage(OPTION_ID, "es");
    }

    @Test
    void findById_withNonDefaultLanguage_noTranslation_shouldUseFallback() {
        // Given - order stored in default language, but translation doesn't exist
        Order order = new Order(
                List.of(new CartItem("item1", "prod1", "Burger", "Delicious", "img.jpg",
                        1, List.of(new ProductCustomizer(OPTION_ID, "Cheese", BigDecimal.valueOf(1.5), 1)),
                        BigDecimal.TEN)),
                1, OrderPaymentStatus.PAID, OrderChannel.WAITER, "pt"
        );
        OrderEntity entity = createOrderEntity(order);
        when(repository.findById("order-1")).thenReturn(Optional.of(entity));

        // Mock: no translation found
        when(optionTranslationRepository.findByOptionIdAndLanguage(OPTION_ID, "pt"))
                .thenReturn(Optional.empty());

        // When
        Optional<Order> result = service.findById("order-1");

        // Then - verify fallback to default language
        assertThat(result).isPresent();
        assertThat(result.get().items().get(0).customizations().get(0).name()).isEqualTo("Cheese");

        verify(optionTranslationRepository).findByOptionIdAndLanguage(OPTION_ID, "pt");
    }

    @Test
    void findAll_shouldTranslateAllOrders() {
        // Given - two orders, one in default language, one in Spanish
        Order order1 = new Order(
                List.of(new CartItem("item1", "prod1", "Burger", "Delicious", "img.jpg",
                        1, List.of(new ProductCustomizer(OPTION_ID, "Cheese", BigDecimal.valueOf(1.5), 1)),
                        BigDecimal.TEN)),
                1, OrderPaymentStatus.PAID, OrderChannel.WAITER, DEFAULT_LANG
        );
        Order order2 = new Order(
                List.of(new CartItem("item2", "prod2", "Pizza", "Tasty", "img2.jpg",
                        1, List.of(new ProductCustomizer(OPTION_ID, "Cheese", BigDecimal.valueOf(1.5), 1)),
                        BigDecimal.valueOf(15))),
                2, OrderPaymentStatus.PAID, OrderChannel.ONLINE, "es"
        );

        OrderEntity entity1 = createOrderEntity(order1);
        OrderEntity entity2 = createOrderEntity(order2);
        when(repository.findAll()).thenReturn(List.of(entity1, entity2));

        CustomizationOptionTranslationEntity translation = new CustomizationOptionTranslationEntity();
        translation.setLanguage("es");
        translation.setName("Queso");
        when(optionTranslationRepository.findByOptionIdAndLanguage(OPTION_ID, "es"))
                .thenReturn(Optional.of(translation));

        // When
        List<Order> result = service.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).items().get(0).customizations().get(0).name()).isEqualTo("Cheese");
        assertThat(result.get(1).items().get(0).customizations().get(0).name()).isEqualTo("Queso");

        verify(optionTranslationRepository).findByOptionIdAndLanguage(OPTION_ID, "es");
    }

    @Test
    void delete_existingOrder_shouldReturnTrue() {
        // Given
        when(repository.existsById("order-1")).thenReturn(true);

        // When
        boolean result = service.delete("order-1");

        // Then
        assertThat(result).isTrue();
        verify(repository).deleteById("order-1");
    }

    @Test
    void delete_nonExistingOrder_shouldReturnFalse() {
        // Given
        when(repository.existsById("order-1")).thenReturn(false);

        // When
        boolean result = service.delete("order-1");

        // Then
        assertThat(result).isFalse();
        verify(repository, never()).deleteById(any());
    }

    private OrderEntity createOrderEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.id());
        entity.setOrderNumber(order.orderNumber());
        entity.setCreatedAt(order.createdAt());
        entity.setUpdatedAt(order.updatedAt());
        entity.setStatus(order.status());
        entity.setTotal(order.total());
        entity.setCancelReason(order.cancelReason());
        entity.setPaymentStatus(order.paymentStatus());
        entity.setOrderChannel(order.orderChannel());
        entity.setOrderLanguage(order.orderLanguage());

        if (order.items() != null) {
            List<CartItemEntity> itemEntities = order.items().stream()
                    .map(item -> {
                        CartItemEntity itemEntity = new CartItemEntity();
                        itemEntity.setId(item.id());
                        itemEntity.setItemId(item.itemId());
                        itemEntity.setName(item.name());
                        itemEntity.setDescription(item.description());
                        itemEntity.setImage(item.image());
                        itemEntity.setQuantity(item.quantity());
                        itemEntity.setPrice(item.price());

                        if (item.customizations() != null) {
                            List<ProductCustomizerEntity> customizerEntities = item.customizations().stream()
                                    .map(customizer -> {
                                        ProductCustomizerEntity customizerEntity = new ProductCustomizerEntity();
                                        customizerEntity.setId(customizer.id());
                                        customizerEntity.setName(customizer.name());
                                        customizerEntity.setPrice(customizer.price());
                                        customizerEntity.setQuantity(customizer.quantity());
                                        return customizerEntity;
                                    })
                                    .toList();
                            itemEntity.setCustomizations(new ArrayList<>(customizerEntities));
                        }

                        return itemEntity;
                    })
                    .toList();
            entity.setItems(new ArrayList<>(itemEntities));
        }

        return entity;
    }
}
