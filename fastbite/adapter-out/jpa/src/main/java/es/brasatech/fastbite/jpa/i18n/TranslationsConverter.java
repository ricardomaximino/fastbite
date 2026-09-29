package es.brasatech.fastbite.jpa.i18n;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores {@link Translations} as JSON text, which works the same on H2 and PostgreSQL.
 */
@Converter
public class TranslationsConverter implements AttributeConverter<Map<String, Map<String, String>>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Map<String, String>>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(Map<String, Map<String, String>> translations) {
        if (translations == null || translations.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(translations);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot store translations", e);
        }
    }

    @Override
    public Map<String, Map<String, String>> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(json, TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot read translations", e);
        }
    }
}
