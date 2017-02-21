package ru.inovus.egisz.medorg.exceptions;

/**
 * Используется для выброса в случае, если не удаётся извлечь требуемый параметр из JMS-сообщения в MDB
 */
public class MessageDrivenValidationException extends Exception{

    public MessageDrivenValidationException(Throwable cause) {
        super(cause);
    }

    public MessageDrivenValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageDrivenValidationException(String message) {
        super(message);
    }
}
