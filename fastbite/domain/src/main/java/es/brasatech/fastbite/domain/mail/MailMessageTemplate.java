package es.brasatech.fastbite.domain.mail;

import java.util.Map;

public record MailMessageTemplate(String name, Map<String, Object> context) {
}
