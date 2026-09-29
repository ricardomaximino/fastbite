package es.brasatech.fastbite.jpa.product;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.domain.product.ProductI18n;
import es.brasatech.fastbite.jpa.i18n.Translations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * JPA implementation of ProductService. Default-language texts live in the entity's columns,
 * other languages in its translations column.
 */
@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class ProductServiceJpaImpl implements ProductService {

    private final ProductJpaRepository repository;
    private final I18nConfig i18nConfig;

    @Override
    public List<ProductDto> findAll() {
        return findAllInLocale(LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public Optional<ProductDto> findById(String id) {
        return findByIdInLocale(id, LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public List<ProductDto> findAllInLocale(String locale) {
        return repository.findAll().stream().map(entity -> toDto(entity, locale)).toList();
    }

    @Override
    public Optional<ProductDto> findByIdInLocale(String id, String locale) {
        return repository.findById(id).map(entity -> toDto(entity, locale));
    }

    @Override
    public ProductDto create(ProductDto productDto) {
        ProductEntity entity = new ProductEntity(null, productDto.name(), productDto.price(), productDto.description(),
                productDto.image(), customizationsOf(productDto.customizations()), productDto.active());
        return toDto(repository.save(entity), i18nConfig.getDefaultLanguage());
    }

    @Override
    public Optional<ProductDto> update(String id, ProductDto productDto) {
        return repository.findById(id).map(entity -> {
            entity.setName(productDto.name());
            entity.setPrice(productDto.price());
            entity.setDescription(productDto.description());
            entity.setImage(productDto.image());
            entity.setCustomizations(customizationsOf(productDto.customizations()));
            entity.setActive(productDto.active());
            return toDto(repository.save(entity), i18nConfig.getDefaultLanguage());
        });
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
    public Optional<ProductI18n> findI18nById(String id) {
        return repository.findById(id).map(this::toI18nDto);
    }

    @Override
    public Optional<ProductI18n> updateI18n(String id, ProductI18n productI18n) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        return repository.findById(id).map(entity -> {
            entity.setName(productI18n.name().get(defaultLang, defaultLang));
            entity.setPrice(productI18n.price());
            entity.setDescription(productI18n.description().get(defaultLang, defaultLang));
            entity.setImage(productI18n.image());
            entity.setCustomizations(customizationsOf(productI18n.customizations()));
            entity.setActive(productI18n.active());
            entity.setTranslations(Translations.of(defaultLang,
                    Map.of("name", productI18n.name(), "description", productI18n.description())));
            return toI18nDto(repository.save(entity));
        });
    }

    private ProductDto toDto(ProductEntity entity, String language) {
        var translations = entity.getTranslations();
        return new ProductDto(
                entity.getId(),
                Translations.get(translations, language, "name", entity.getName()),
                entity.getPrice(),
                Translations.get(translations, language, "description", entity.getDescription()),
                entity.getImage(),
                entity.getCustomizations(),
                entity.isActive());
    }

    private ProductI18n toI18nDto(ProductEntity entity) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        var translations = entity.getTranslations();
        return new ProductI18n(
                entity.getId(),
                Translations.toI18nField(translations, "name", defaultLang, entity.getName()),
                entity.getPrice(),
                Translations.toI18nField(translations, "description", defaultLang, entity.getDescription()),
                entity.getImage(),
                entity.getCustomizations(),
                entity.isActive());
    }

    private static Set<String> customizationsOf(Set<String> customizations) {
        return customizations != null ? new HashSet<>(customizations) : new HashSet<>();
    }
}
