package ru.inovus.egisz.medorg.service;

import ru.inovus.egisz.medorg.datatypes.ConsumerRelate;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ConsumerRelateExtractor {

    private static ConsumerRelateExtractorBean consumerRelateExtractor;

    static {
        try {
            consumerRelateExtractor = (ConsumerRelateExtractorBean) new InitialContext().lookup("java:app/medorg-adapter/ConsumerRelateExtractorBean");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConsumerRelate extractInfo(final String id){
        return consumerRelateExtractor.extractInfo(id);
    }

    public static ConsumerRelate extractInfoByUsername(final String username){
        return consumerRelateExtractor.extractInfoByUsername(username);
    }
}
