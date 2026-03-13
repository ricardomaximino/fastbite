package es.brasatech.fastbite.domain.exception;

public class FastbiteException extends Exception {

    public FastbiteException(String message) {
        super(message);
    }

    public FastbiteException(Exception exception) {
        super(exception);
    }

    public FastbiteException(String message, Exception exception) {
        super(message, exception);
    }
}
