package es.brasatech.fastbite.dto.office;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic DTO wrapper for BackOffice entities.
 * The frontend expects responses with 'id' and 'customFields' properties.
 *
 * @param <T> The type of the entity being wrapped
 */
public record BackOfficeDto<T>(
        String id,
        @JsonProperty("customFields") T customFields
) {
    public static <T> BackOfficeDto<T> of(String id, T customFields) {
        return new BackOfficeDto<>(id, customFields);
    }
}
