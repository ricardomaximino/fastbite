package es.brasatech.fastbite.jpa.product;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.jpa.i18n.Translations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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
        String language = LocaleContextHolder.getLocale().getLanguage();
        return repository.findAll().stream().map(entity -> toDto(entity, language)).toList();
    }

    @Override
    public Optional<ProductDto> findById(String id) {
        return repository.findById(id).map(entity -> toDto(entity, LocaleContextHolder.getLocale().getLanguage()));
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

    private static Set<String> customizationsOf(Set<String> customizations) {
        return customizations != null ? new HashSet<>(customizations) : new HashSet<>();
    }
}
