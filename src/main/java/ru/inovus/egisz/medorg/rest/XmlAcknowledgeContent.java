package ru.inovus.egisz.medorg.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Класс, описывающий структуру контента в ответных сообщениях REST
 */
@XmlRootElement(name = "content")
public class XmlAcknowledgeContent {

    @XmlElement(name = "success")
    private boolean success;

    @XmlElement(name = "id")
    private String id;

    public XmlAcknowledgeContent() {}

    public XmlAcknowledgeContent(String id) {
        this.success = true;
        this.id = id;
    }
}