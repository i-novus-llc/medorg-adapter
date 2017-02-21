package ru.inovus.egisz.medorg.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Класс, описывающий структуру контента в ответных сообщениях REST
 */
@XmlRootElement(name = "content")
public class XmlFailContent {

    @XmlElement(name = "success")
    private boolean success;

    @XmlElement(name = "message")
    private String message;

    public XmlFailContent() {}

    public XmlFailContent(String message) {
        this.success = false;
        this.message = message;
    }
}
