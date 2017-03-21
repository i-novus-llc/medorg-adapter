package ru.inovus.egisz.medorg.exceptions;

/**
 * Используется для выброса в случае, если не удалось доставить потребителю результирующее сообщение ЕГИСЗ ИПС
 */
public class NotDeliveredException extends RuntimeException {

    public NotDeliveredException(String message) {
        super(message);
    }

    public NotDeliveredException(Throwable cause) {
        super(cause);
    }
}
