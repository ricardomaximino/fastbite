package es.brasatech.fastbite.office.i18n;

import es.brasatech.fastbite.domain.I18nField;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for I18nField.
 * Tests the automatic fallback strategy for translations.
 */
class I18nFieldTest {

    @Test
    void testCreateI18nFieldWithDefaultLocale() {
        I18nField field = I18nField.of("en", "Hello");

        assertEquals("Hello", field.get("en", "en"));
        assertNotNull(field.getAll());
        assertEquals(1, field.getAll().size());
    }

    @Test
    void testGetWithExactLocaleMatch() {
        I18nField field = new I18nField(Map.of(
                "en", "Hello",
                "es", "Hola",
                "pt", "Olá"
        ));

        assertEquals("Hello", field.get("en", "en"));
        assertEquals("Hola", field.get("es", "en"));
        assertEquals("Olá", field.get("pt", "en"));
    }

    @Test
    void testFallbackToDefaultLocale() {
        I18nField field = new I18nField(Map.of(
                "en", "Hello",
                "es", "Hola"
        ));

        // Request non-existent locale, should fallback to default
        assertEquals("Hello", field.get("fr", "en"));
        assertEquals("Hello", field.get("de", "en"));
    }

    @Test
    void testFallbackToAnyAvailable() {
        I18nField field = new I18nField(Map.of(
                "es", "Hola",
                "pt", "Olá"
        ));

        // Request non-existent locale with non-existent default, should return any available
        String result = field.get("fr", "en");
        assertTrue(result.equals("Hola") || result.equals("Olá"));
    }

    @Test
    void testEmptyField() {
        I18nField field = new I18nField(Map.of());

        assertEquals("", field.get("en", "en"));
        assertEquals("", field.get("es", "en"));
    }

    @Test
    void testSetTranslation() {
        I18nField field = I18nField.of("en", "Hello");

        field.set("es", "Hola");
        field.set("pt", "Olá");

        assertEquals("Hello", field.get("en", "en"));
        assertEquals("Hola", field.get("es", "en"));
        assertEquals("Olá", field.get("pt", "en"));
    }

    @Test
    void testUpdateExistingTranslation() {
        I18nField field = I18nField.of("en", "Hello");

        field.set("en", "Hi");

        assertEquals("Hi", field.get("en", "en"));
    }

    @Test
    void testEmptyStringTranslation() {
        I18nField field = new I18nField(Map.of(
                "en", "Hello",
                "es", "",
                "pt", "Olá"
        ));

        // Empty string for 'es' should fallback to default 'en'
        assertEquals("Hello", field.get("es", "en"));
    }

    @Test
    void testNullTranslation() {
        I18nField field = new I18nField(Map.of("en", "Hello"));
        field.getAll().put("es", null);

        // Null translation should fallback to default
        assertEquals("Hello", field.get("es", "en"));
    }

    @Test
    void testGetAllTranslations() {
        Map<String, String> translations = Map.of(
                "en", "Hello",
                "es", "Hola",
                "pt", "Olá"
        );
        I18nField field = new I18nField(translations);

        Map<String, String> allTranslations = field.getAll();
        assertEquals(3, allTranslations.size());
        assertEquals("Hello", allTranslations.get("en"));
        assertEquals("Hola", allTranslations.get("es"));
        assertEquals("Olá", allTranslations.get("pt"));
    }

    @Test
    void testCopyConstructor() {
        I18nField original = new I18nField(Map.of(
                "en", "Hello",
                "es", "Hola"
        ));

        I18nField copy = new I18nField(original.getAll());

        assertEquals(original.get("en", "en"), copy.get("en", "en"));
        assertEquals(original.get("es", "en"), copy.get("es", "en"));
    }

    @Test
    void testSpecificLocaleWithCountryCode() {
        I18nField field = new I18nField(Map.of(
                "en", "Hello",
                "pt", "Olá",
                "pt_BR", "Olá Brasil"
        ));

        // Exact match for specific locale
        assertEquals("Olá Brasil", field.get("pt_BR", "en"));

        // General locale
        assertEquals("Olá", field.get("pt", "en"));

        // Fallback to default when specific locale doesn't exist
        assertEquals("Hello", field.get("pt_PT", "en"));
    }
}
