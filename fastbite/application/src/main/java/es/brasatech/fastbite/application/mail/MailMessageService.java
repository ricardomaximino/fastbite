package es.brasatech.fastbite.application.mail;

import es.brasatech.fastbite.domain.mail.MailMessage;

import java.util.Optional;

public interface MailMessageService {

    Optional<MailMessage> findById(String id);
    void saveMailMessage(MailMessage mailMessage);
    void updateMailMessage(MailMessage mailMessage);
    void deleteMailMessage(String mailMessageId);

}
