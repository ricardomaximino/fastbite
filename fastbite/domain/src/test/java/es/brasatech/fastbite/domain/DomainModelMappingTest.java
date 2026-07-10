package es.brasatech.fastbite.domain;

import es.brasatech.fastbite.domain.customization.Customization;
import es.brasatech.fastbite.domain.customization.CustomizationDto;
import es.brasatech.fastbite.domain.customization.CustomizationOption;
import es.brasatech.fastbite.domain.customization.CustomizationOptionDto;
import es.brasatech.fastbite.domain.product.Product;
import es.brasatech.fastbite.domain.product.ProductDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelMappingTest {

    @Test
    void productDto_shouldMapToAndFromDomain() {
        ProductDto dto = new ProductDto(
                "p-1",
                "Kebab",
                BigDecimal.valueOf(8.50),
                "Tasty Kebab",
                "kebab.jpg",
                Set.of("cust-1"),
                true
        );

        Product domain = dto.toDomain();
        assertThat(domain.getId()).isEqualTo("p-1");
        assertThat(domain.getName()).isEqualTo("Kebab");
        assertThat(domain.getPrice()).isEqualTo(BigDecimal.valueOf(8.50));
        assertThat(domain.getDescription()).isEqualTo("Tasty Kebab");
        assertThat(domain.getImage()).isEqualTo("kebab.jpg");
        assertThat(domain.getCustomizations()).containsExactlyInAnyOrderElementsOf(Set.of("cust-1"));
        assertThat(domain.isActive()).isTrue();

        ProductDto mappedBack = ProductDto.fromDomain(domain);
        assertThat(mappedBack).isEqualTo(dto);
    }

    @Test
    void customizationDto_shouldMapToAndFromDomain() {
        CustomizationOptionDto optDto = new CustomizationOptionDto(
                "opt-1",
                "Garlic Sauce",
                BigDecimal.valueOf(0.50),
                true,
                1
        );
        CustomizationDto dto = new CustomizationDto(
                "c-1",
                "Sauces",
                "checkbox",
                List.of(optDto),
                5
        );

        Customization domain = dto.toDomain();
        assertThat(domain.getId()).isEqualTo("c-1");
        assertThat(domain.getName()).isEqualTo("Sauces");
        assertThat(domain.getType()).isEqualTo("checkbox");
        assertThat(domain.getUsageCount()).isEqualTo(5);
        assertThat(domain.getOptions()).hasSize(1);
        
        CustomizationOption domainOpt = domain.getOptions().get(0);
        assertThat(domainOpt.getId()).isEqualTo("opt-1");
        assertThat(domainOpt.getName()).isEqualTo("Garlic Sauce");
        assertThat(domainOpt.getPrice()).isEqualTo(BigDecimal.valueOf(0.50));
        assertThat(domainOpt.isSelectedByDefault()).isTrue();
        assertThat(domainOpt.getDefaultValue()).isEqualTo(1);

        CustomizationDto mappedBack = CustomizationDto.fromDomain(domain);
        assertThat(mappedBack).isEqualTo(dto);
    }
}
