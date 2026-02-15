package es.brasatech.fastbite.domain.table;

import es.brasatech.fastbite.domain.I18nField;

public record TableI18n(String id, I18nField name, int seats, TableStatus status, boolean active) {
}
