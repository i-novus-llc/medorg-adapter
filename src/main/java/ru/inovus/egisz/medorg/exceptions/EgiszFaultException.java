package ru.inovus.egisz.medorg.exceptions;

/**
 * Используется для выброса в случае, если отказано в обработке запроса ЕГИСЗ ИПС
 */
public class EgiszFaultException extends Exception{

    public EgiszFaultException(String message, Throwable cause) {
        super(message, cause);
    }
}
