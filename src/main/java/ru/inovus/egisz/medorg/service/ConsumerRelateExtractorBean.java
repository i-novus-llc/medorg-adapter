package ru.inovus.egisz.medorg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inovus.egisz.medorg.datatypes.ConsumerRelate;
import ru.inovus.egisz.medorg.rest.RestCallbackCommand;
import ru.inovus.egisz.medorg.util.ConfigHelper;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class ConsumerRelateExtractorBean {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerRelateExtractorBean.class);

    @EJB
    private MessageInfoService messageInfoService;

    public ConsumerRelate extractInfo(final String id) {

        ConsumerRelate consumerRelate = null;

        final RestCallbackCommand restCallbackInfo = getRestCallbackInfo(id);

        if (restCallbackInfo != null) {

            final String authorizedUserName = restCallbackInfo.getAuthorizedUserName();

            consumerRelate = extractInfoByUsername(authorizedUserName);
        }

        return consumerRelate;
    }

    public ConsumerRelate extractInfoByUsername(final String authorizedUserName) {
        /* извлекаем из настроек название ИС */
        String informationSystemName = ConfigHelper.getAppProperty(authorizedUserName + ".informationSystemName");

        /* извлекаем из настроек PEM-сертификата ЭП */
        String pemCertificate = ConfigHelper.getAppProperty(authorizedUserName + ".pemCertificate");

        /* извлекаем из настроек PEM-закрытого ключа ЭП */
        String pemPrivateKey = ConfigHelper.getAppProperty(authorizedUserName + ".pemPrivateKey");

        return new ConsumerRelate(informationSystemName, pemCertificate, pemPrivateKey);
    }

    /**
     * Возвращает restCallbackUrl для id принятого сообщения ЕГИСЗ ИПС
     *
     * @param egiszRespMessageId id принятого сообщения ЕГИСЗ ИПС
     * @return
     */
    private RestCallbackCommand getRestCallbackInfo(final String egiszRespMessageId) {

        RestCallbackCommand result = messageInfoService.getConsumerByMessageId(egiszRespMessageId);

        if (result == null) {
            logger.warn("MEDORG. Не удалось найти привязку к restCallbackUrl для id принятого сообщения ЕГИСЗ ИПС {}", egiszRespMessageId);
        }

        return result;
    }
}
