package ru.inovus.egisz.medorg.exceptions;

public class ConsumerHttpBadGatewayException extends RuntimeException{
    public ConsumerHttpBadGatewayException(String message) {
        super(message);
    }
}
