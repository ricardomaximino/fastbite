package es.brasatech.fastbite.application.mail;

import es.brasatech.fastbite.domain.mail.Email;

import java.util.Optional;

public interface EmailRepository {

    Optional<Email> find(String email);

    void save(Email email);
}
