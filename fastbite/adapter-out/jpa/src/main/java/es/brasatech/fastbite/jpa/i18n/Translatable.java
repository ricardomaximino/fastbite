package es.brasatech.fastbite.jpa.i18n;

import java.util.Map;

/**
 * An entity whose text fields have translations: language -> field -> text (see {@link Translations}).
 */
public interface Translatable {

    Map<String, Map<String, String>> getTranslations();

    void setTranslations(Map<String, Map<String, String>> translations);
}
