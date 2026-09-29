package es.brasatech.fastbite.jpa.i18n;

import es.brasatech.fastbite.domain.I18nField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationsTest {

    private static final Map<String, Map<String, String>> STORED = Map.of(
            "es", Map.of("name", "Salsa", "description", ""),
            "pt", Map.of("name", "Molho"));

    @Test
    void readsTheRequestedLanguageAndFallsBackToTheDefault() {
        assertThat(Translations.get(STORED, "es", "name", "Sauce")).isEqualTo("Salsa");
        assertThat(Translations.get(STORED, "es", "description", "Garlic sauce")).as("blank").isEqualTo("Garlic sauce");
        assertThat(Translations.get(STORED, "fr", "name", "Sauce")).as("missing language").isEqualTo("Sauce");
        assertThat(Translations.get(null, "es", "name", "Sauce")).isEqualTo("Sauce");
    }

    @Test
    void buildsAllLanguagesOfAFieldIncludingTheDefault() {
        assertThat(Translations.toI18nField(STORED, "name", "en", "Sauce").getAll())
                .isEqualTo(Map.of("en", "Sauce", "es", "Salsa", "pt", "Molho"));
    }

    @Test
    void storesOnlyNonDefaultLanguagesWithText() {
        Map<String, String> description = new HashMap<>(Map.of("en", "Garlic sauce", "es", "Salsa de ajo"));
        var translations = Translations.of("en", Map.of(
                "name", new I18nField(Map.of("en", "Sauce", "es", "Salsa", "pt", "  ")),
                "description", new I18nField(description)));

        assertThat(translations).isEqualTo(Map.of("es", Map.of("name", "Salsa", "description", "Salsa de ajo")));
    }

    @Test
    void theConverterRoundTripsAndStoresNothingForNoTranslations() {
        var converter = new TranslationsConverter();
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(STORED))).isEqualTo(STORED);
        assertThat(converter.convertToDatabaseColumn(Map.of())).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
    }
}
