package es.brasatech.fastbite.domain;

/**
 * One translatable text of an item, in all its languages.
 *
 * @param key   identifies the text within the item: "name", "description" or "option_{optionId}"
 * @param kind  what the text is: {@link #NAME}, {@link #DESCRIPTION} or {@link #OPTION} (an option's name)
 * @param value the text in every language, including the default one
 */
public record TranslatableText(String key, String kind, I18nField value) {

    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String OPTION = "option";
}
