package ru.inovus.egisz.medorg.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Утилита для работы с настройками
 */
public class ConfigHelper {

    /**
     * Возвращает объект Properties настроек приложения
     * @return
     * @throws RuntimeException
     */
    public static Properties getApplicationProperties() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("medorg-adapter.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {/* NOP */}
        }
        return properties;
    }

    /**
     * Возвращает значение свойства настройки приложения
     * @param property
     * @return
     * @throws RuntimeException
     */
    public static String getAppProperty(final String property){
        Properties appConf = ConfigHelper.getApplicationProperties();
        return appConf.getProperty(property);
    }
}
