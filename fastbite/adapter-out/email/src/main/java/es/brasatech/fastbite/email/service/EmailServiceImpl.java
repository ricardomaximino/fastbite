package es.brasatech.fastbite.email.service;

import es.brasatech.fastbite.application.mail.EmailRepository;
import es.brasatech.fastbite.application.mail.EmailService;
import es.brasatech.fastbite.domain.event.MailMessageSentEvent;
import es.brasatech.fastbite.domain.exception.FastbiteException;
import es.brasatech.fastbite.domain.mail.Email;
import es.brasatech.fastbite.domain.mail.MailMessage;
import es.brasatech.fastbite.domain.mail.MailMessageAttachment;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String UTF_8 = "UTF-8";
    public static final String E_MAIL = "E-mail: ";
    public static final String WAS_SENT_SUCCESSFULLY = " was sent successfully!";
    @Value("${spring.mail.username}")
    private String from;
    @Value("${company.mail.to}")
    private String to;

    private final EmailRepository emailRepository;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final ApplicationEventPublisher applicationEventPublisher;



    @Override
    public void sendTextEmail(String id, String to, String subject, String text, MailMessageAttachment attachment) throws FastbiteException {
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true, UTF_8);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            if (attachment != null) {
                FileSystemResource fileSystemResource = new FileSystemResource(new File("classpath:/resources/" + attachment));
                helper.addAttachment(fileSystemResource.getFilename(), fileSystemResource);
            }
            mailSender.send(helper.getMimeMessage());
            logSuccessfullySentMessage(id);
        } catch (MessagingException exception) {
            throw new FastbiteException(exception);
        }
    }

    @Override
    public void sendHTMLEmail(String id, String to, String subject, String htmlBody, MailMessageAttachment attachment) throws FastbiteException {
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true, UTF_8);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if(attachment != null) {
                ClassPathResource classPathResource = new ClassPathResource(attachment.name());
                helper.addAttachment(Objects.requireNonNull(classPathResource.getFilename()), classPathResource);
            }
            mailSender.send(helper.getMimeMessage());
            logSuccessfullySentMessage(id);
        } catch (MessagingException exception) {
            throw new FastbiteException(exception);
        }
    }

    @Override
    public void sendEmail(MailMessage message) throws FastbiteException {
        if(message.template() != null) {
            Context context = new Context(new Locale(message.lang()), message.template().context());
            String htmlBody = templateEngine.process(message.template().name(), context);
            sendHTMLEmail(message.id(), message.email().value(), message.subject(), htmlBody, message.attachment());
        } else {
            sendTextEmail(message.id(), message.email().value(), message.subject(), message.text(), message.attachment());
        }
    }

    @Override
    public boolean emailIsValid(String email) {
        return emailRepository.find(email).map(Email::isValid).filter(obj -> true).filter(v -> v.equals(Boolean.TRUE)).orElse(false);
    }

    private void logSuccessfullySentMessage(String id) {
        log.debug(E_MAIL + "{}" + WAS_SENT_SUCCESSFULLY, id);
        applicationEventPublisher.publishEvent(new MailMessageSentEvent(id));

    }
}
