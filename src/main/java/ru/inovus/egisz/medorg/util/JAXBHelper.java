package ru.inovus.egisz.medorg.util;

import org.apache.cxf.jaxb.JAXBDataBinding;

import javax.xml.bind.JAXBException;

public class JAXBHelper {

    public static JAXBDataBinding getJAXBDataBinding(){
        try {
            return new JAXBDataBinding(String.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
