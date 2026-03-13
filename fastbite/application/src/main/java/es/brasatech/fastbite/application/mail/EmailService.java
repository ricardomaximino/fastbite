package es.brasatech.fastbite.application.mail;

import es.brasatech.fastbite.domain.exception.FastbiteException;
import es.brasatech.fastbite.domain.mail.MailMessage;
import es.brasatech.fastbite.domain.mail.MailMessageAttachment;

import java.io.IOException;

public interface EmailService {
    void sendTextEmail(String id, String to, String subject, String text, MailMessageAttachment attachment) throws FastbiteException;
    void sendHTMLEmail(String id, String to, String subject, String htmlBody, MailMessageAttachment attachment) throws FastbiteException, IOException;
    void sendEmail(MailMessage message) throws FastbiteException;
    boolean emailIsValid(String email);
}
