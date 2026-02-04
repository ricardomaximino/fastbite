package es.brasatech.fastbite.domain;

/**
 * Information about a locale for UI display.
 */
public record LocaleInfo(
                String code, // e.g., "en", "pt_PT", "pt_BR"
                String displayName, // e.g., "English", "Português (Portugal)", "Português (Brasil)"
                String nativeName // e.g., "English", "Português (Portugal)", "Português (Brasil)"
) {
}
