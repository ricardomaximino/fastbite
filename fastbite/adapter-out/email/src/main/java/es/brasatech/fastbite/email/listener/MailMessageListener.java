package es.brasatech.fastbite.email.listener;

import es.brasatech.fastbite.application.mail.EmailService;
import es.brasatech.fastbite.application.mail.MailMessageService;
import es.brasatech.fastbite.domain.event.MailMessageCreatedEvent;
import es.brasatech.fastbite.domain.event.MailMessageSentEvent;
import es.brasatech.fastbite.domain.event.MailMessageValidatedEvent;
import es.brasatech.fastbite.domain.event.MailMessageValidationRequestedEvent;
import es.brasatech.fastbite.domain.exception.FastbiteException;
import es.brasatech.fastbite.domain.mail.MailMessage;
import es.brasatech.fastbite.domain.mail.MailMessageStatus;
import es.brasatech.fastbite.domain.mail.MailMessageTemplate;
import es.brasatech.fastbite.domain.mail.MailMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailMessageListener {

    private final EmailService emailService;
    private final MailMessageService mailMessageService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    public void mailMessageCreated(MailMessageCreatedEvent event) {
        var mailMessage = mailMessageService.findById(event.mailMessageId());
        if (mailMessage.isPresent()) {
            if (emailService.emailIsValid(mailMessage.get().email().value())){
                applicationEventPublisher.publishEvent(new MailMessageValidatedEvent(event.mailMessageId()));
            } else {
                applicationEventPublisher.publishEvent(new MailMessageValidationRequestedEvent(event.mailMessageId()));
            }
        }
    }

    @EventListener
    public void mailMessageValidated(MailMessageValidatedEvent event) {
        var mailMessage = mailMessageService.findById(event.mailMessageId());
        if (mailMessage.isPresent()) {
            try {
                emailService.sendEmail(mailMessage.get());
            } catch (FastbiteException exception) {
                log.error(exception.getMessage());
            }
        }
    }

    @EventListener
    public void mailMessageSent(MailMessageSentEvent event) {
        var mailMessage = mailMessageService.findById(event.mailMessageId());
        if (mailMessage.isPresent() && MailMessageType.INSCRIPTION.equals(mailMessage.get().type())) {
            var message = mailMessage.get();
            var welcomeMailMessage = createWelcomeMailMessage(message);
            mailMessageService.saveMailMessage(welcomeMailMessage);
            try {
                emailService.sendEmail(welcomeMailMessage);
            } catch (FastbiteException exception) {
                log.error(exception.getMessage());
            }
        }
    }

    @EventListener
    public void mailMessageValidationRequested(MailMessageValidationRequestedEvent event) {
        var mailMessage = mailMessageService.findById(event.mailMessageId());
        if (mailMessage.isPresent()) {
            var message = mailMessage.get();
            var validationMailMessage = createValidationMailMessage(message);
            mailMessageService.saveMailMessage(validationMailMessage);
            try {
                emailService.sendEmail(validationMailMessage);
            } catch (FastbiteException exception) {
                log.error(exception.getMessage());
            }
        }
    }

    private MailMessage createWelcomeMailMessage(MailMessage message) {
        return new MailMessage(
            message.name(),
            message.email(),
            "Welcome",
            null,
            message.lang(),
            new MailMessageTemplate("email/fastbite-welcome.html", Map.of("name", message.name())),
            null,
            MailMessageType.WELCOME,
            MailMessageStatus.VALID);
    }

    private MailMessage createValidationMailMessage(MailMessage message) {
        Map<String, Object> context = Map.of("name", message.name(), "link","linkValue", "linkText", "linkTextValue");
        return new MailMessage(
                message.name(),
                message.email(),
                "E-Mail Validation",
                null,
                message.lang(),
                new MailMessageTemplate("email/fastbite-email-validation.html", context),
                null,
                MailMessageType.WELCOME,
                MailMessageStatus.VALID);
    }

}
