package ru.inovus.egisz.medorg.api.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Типы SOAP-сообщений при информационном взаимодействии с ЕГИСЗ ИПС
 */
public enum SOAPMessageType {

    /* запрос */
    SEND_DOCUMENT("sendDocument"),

    /* id принятого сообщения */
    SEND_DOCUMENT_RESPONSE("sendDocumentResponse"),

    /* результирующее сообщение ЕГИСЗ ИПС */
    SEND_RESPONSE("sendResponse"),

    /* подтверждение о получении результирующего сообщения */
    SEND_RESPONSE_RESPONSE("sendResponseResponse");

    private final String value;

    // Reverse-lookup map for getting a day from an value
    private static final Map<String, SOAPMessageType> lookup = new HashMap<>();

    static {
        for (SOAPMessageType d : SOAPMessageType.values()) {
            lookup.put(d.getValue().toLowerCase(), d);
        }
    }

    public static SOAPMessageType get(String value) {
        return value != null ? lookup.get((value.matches(".+:.+") ? value.split(":")[1] : value).toLowerCase()) : null;
    }

    SOAPMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String getMessageExchangeNote(String nodeTagName){
        return getMessageExchangeNote(SOAPMessageType.get(nodeTagName));
    }

    public static String getMessageExchangeNote(SOAPMessageType soapMessageType){

        String result = StringUtils.EMPTY;

        if(soapMessageType == SEND_DOCUMENT){
            result = "сообщение запроса";
        } else if (soapMessageType == SEND_DOCUMENT_RESPONSE){
            result = "id принятого сообщения";
        } else if(soapMessageType == SEND_RESPONSE){
            result = "результирующее сообщение";
        } else if (soapMessageType == SEND_RESPONSE_RESPONSE) {
            result = "подтверждение о получении результирующего сообщения";
        }

        return result;
    }
}
