package es.brasatech.fastbite.domain.customization;

import java.util.List;

/**
 * DTO for Customization management in BackOffice.
 */
public record CustomizationDto(
                String id,
                String name,
                String type, // radio, checkbox, quantity
                List<CustomizationOptionDto> options,
                int usageCount) {

    public Customization toDomain() {
        List<CustomizationOption> domainOptions = options != null
                ? options.stream().map(CustomizationOptionDto::toDomain).toList()
                : List.of();
        return new Customization(id, name, type, domainOptions, usageCount);
    }

    public static CustomizationDto fromDomain(Customization domain) {
        if (domain == null) {
            return null;
        }
        List<CustomizationOptionDto> optionDtos = domain.getOptions() != null
                ? domain.getOptions().stream().map(CustomizationOptionDto::fromDomain).toList()
                : List.of();
        return new CustomizationDto(
                domain.getId(),
                domain.getName(),
                domain.getType(),
                optionDtos,
                domain.getUsageCount()
        );
    }
}
