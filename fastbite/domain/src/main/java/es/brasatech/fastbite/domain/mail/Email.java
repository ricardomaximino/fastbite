package es.brasatech.fastbite.domain.mail;

public record Email(String value, Boolean isValid) {

    public Email createValidEmail() {
        return new Email(value, true);
    }
}
