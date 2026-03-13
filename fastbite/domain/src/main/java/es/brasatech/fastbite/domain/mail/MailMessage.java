package es.brasatech.fastbite.domain.mail;

import java.time.LocalDateTime;
import java.util.UUID;

public record MailMessage(String id, String name, Email email, String subject, String text, String lang, MailMessageTemplate template, MailMessageAttachment attachment, MailMessageType type, MailMessageStatus status, LocalDateTime creationDateTime) {

    public MailMessage(String name, Email email, String subject, String text, String lang, MailMessageTemplate template, MailMessageAttachment attachment, MailMessageType type, MailMessageStatus status) {
        this(UUID.randomUUID().toString(), name, email, subject, text, lang, template, attachment, type, status, LocalDateTime.now());
    }

    public MailMessage(String name, Email email, String subject, String text, String lang, MailMessageTemplate template, MailMessageType type, MailMessageStatus status) {
        this(UUID.randomUUID().toString(), name, email, subject, text, lang, template, null, type, status, LocalDateTime.now());
    }
    public MailMessage(String name, Email email, String subject, String text, String lang, MailMessageType type, MailMessageStatus status) {
        this(UUID.randomUUID().toString(), name, email, subject, text, lang, null, null, type, status, LocalDateTime.now());
    }
}
