package es.brasatech.fastbite.email.service;

import es.brasatech.fastbite.application.mail.OwnerSetupMailPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class OwnerSetupMailAdapter implements OwnerSetupMailPort {
    private final JavaMailSender sender;
    private final String from;

    public OwnerSetupMailAdapter(JavaMailSender sender, @Value("${spring.mail.username}") String from) {
        this.sender = sender;
        this.from = from;
    }

    @Override
    public void sendSetupLink(String email, String username, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Set your FastBite password");
        message.setText("Your FastBite owner account is ready.\n\nUsername: " + username
                + "\n\nChoose your password using this single-use link (expires in 24 hours):\n" + link
                + "\n\nYour account cannot sign in until you set a password. If you did not request this account, ignore this email.");
        sender.send(message);
    }
}
