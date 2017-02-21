package ru.inovus.egisz.medorg.callback;

import java.io.Serializable;

/**
 * Класс предназначен для обмена данными результирующего сообщения ЕГИСЗ ИПС
 */
public class ResponseCommand implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String oid;

    private final String response;

    public ResponseCommand(String oid, String response) {
        this.oid = oid;
        this.response = response;
    }

    public String getOid() {
        return oid;
    }

    public String getResponse() {
        return response;
    }
}
