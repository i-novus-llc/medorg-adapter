package ru.inovus.egisz.medorg.callback;

import java.io.Serializable;

/**
 * Класс предназначен для обмена данными результирующего сообщения ЕГИСЗ ИПС
 */
public class ResponseCommand implements Serializable {

    private static final long serialVersionUID = 3L;

    private final String messageId;

    private final String oid;

    private final String response;

    ResponseCommand(String messageId, String oid, String response) {
        this.messageId = messageId;
        this.oid = oid;
        this.response = response;
    }

    public String getOid() {
        return oid;
    }

    public String getResponse() {
        return response;
    }

    public String getMessageId() {
        return messageId;
    }
}
