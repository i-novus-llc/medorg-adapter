package ru.inovus.egisz.medorg;

import org.apache.commons.lang3.CharEncoding;
import org.apache.xml.security.utils.XMLUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.Security;

/**
 * Инициализация приложения
 */
@Startup
@Singleton
public class ApplicationStartup {

    @PostConstruct
    public void init() {

        /* инициализация библиотеки XML-security */
        apacheXmlSecurityInitialize();

        /* инициализация провайдера BouncyCastle */
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        /* настройка кодировки */
        charsetInitialize();
    }

    /**
     * Настройка системной кодировки
     */
    private void charsetInitialize() {

        System.setProperty("file.encoding", CharEncoding.UTF_8);

        try {
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Инициализация библиотеки XML Security
     */
    private void apacheXmlSecurityInitialize() {

        if (!org.apache.xml.security.Init.isInitialized()) {

            System.setProperty("org.apache.xml.security.ignoreLineBreaks", "true");
            org.apache.xml.security.Init.init();

            try {

                Field f = XMLUtils.class.getDeclaredField("ignoreLineBreaks");

                f.setAccessible(true);

            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
