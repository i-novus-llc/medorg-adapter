package ru.inovus.egisz.medorg.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Класс, описывающий структуру контента в ответных сообщениях REST
 */
@XmlRootElement(name = "content")
public class XmlResultContent {

    @XmlElement(name = "success")
    private boolean success;

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "oid")
    private String oid;

    @XmlElement(name = "response")
    private String response;

    public XmlResultContent() {}

    public XmlResultContent(String id, String oid, String response) {
        this.success = true;
        this.id = id;
        this.oid = oid;
        this.response = response;
    }
}
