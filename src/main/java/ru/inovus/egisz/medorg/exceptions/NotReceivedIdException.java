package ru.inovus.egisz.medorg.exceptions;

/**
 * Используется для выброса в случае, если не получен id принятого сообщения ЕГИСЗ ИПС
 */
public class NotReceivedIdException extends Exception {

    public NotReceivedIdException(String message) {
        super(message);
    }
}
