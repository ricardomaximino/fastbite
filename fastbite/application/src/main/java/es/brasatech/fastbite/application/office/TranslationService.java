package es.brasatech.fastbite.application.office;

import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.TranslatableText;
import es.brasatech.fastbite.domain.TranslatableType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads and saves the translatable texts of catalog items (names, descriptions, option names) in all languages.
 */
public interface TranslationService {

    /**
     * The item's translatable texts, each in all its languages; empty if there is no such item.
     */
    Optional<List<TranslatableText>> findTexts(TranslatableType type, String id);

    /**
     * Saves texts by {@link TranslatableText#key()}, in all languages including the default one.
     * Texts that are not given keep their values. Returns false if there is no such item.
     */
    boolean saveTexts(TranslatableType type, String id, Map<String, I18nField> texts);
}
