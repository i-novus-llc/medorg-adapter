package ru.inovus.egisz.medorg.service;

import ru.inovus.egisz.medorg.datatypes.ConsumerRelate;
import ru.inovus.egisz.medorg.exceptions.MessageDrivenValidationException;
import ru.inovus.egisz.medorg.rest.RestCallbackCommand;
import ru.inovus.egisz.medorg.util.ConfigHelper;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Queue;

@Stateless
@LocalBean
public class ConsumerRelateExtractorBean {

    @EJB
    private JmsMessageExtractorBean jmsMessageExtractor;

    @Resource(lookup = "java:jboss/exported/jms/queue/RestCallbackQueue")
    private Queue restCallbackQueue;

    public ConsumerRelate extractInfo(final String id){

        final RestCallbackCommand restCallbackInfo = getRestCallbackInfo(id);

        final String authorizedUserName = restCallbackInfo.getAuthorizedUserName();

        /* извлекаем из настроек название ИС */
        String informationSystemName = ConfigHelper.getAppProperty(authorizedUserName + ".informationSystemName");

         /* извлекаем из настроек PEM-сертификата ЭП */
        String pemCertificate = ConfigHelper.getAppProperty(authorizedUserName+".pemCertificate");

        /* извлекаем из настроек PEM-закрытого ключа ЭП */
        String pemPrivateKey = ConfigHelper.getAppProperty(authorizedUserName+".pemPrivateKey");

        return new ConsumerRelate(authorizedUserName, informationSystemName, pemCertificate, pemPrivateKey);
    }

    /**
     * Возвращает restCallbackUrl для id принятого сообщения ЕГИСЗ ИПС
     *
     * @param egiszRespMessageId id принятого сообщения ЕГИСЗ ИПС
     * @return
     * @throws MessageDrivenValidationException
     */
    private RestCallbackCommand getRestCallbackInfo(final String egiszRespMessageId) {

        final javax.jms.Message jmsMessage = jmsMessageExtractor.getMessage(egiszRespMessageId, restCallbackQueue);

        if (jmsMessage == null) {
            throw new RuntimeException("MEDORG. Не удалось найти привязку к restCallbackUrl в очереди queue/RestCallbackQueue для id принятого сообщения ЕГИСЗ ИПС: " + egiszRespMessageId);
        }

        RestCallbackCommand result;

        try {
            result = jmsMessage.getBody(RestCallbackCommand.class);
        } catch (Exception e) {
            throw new RuntimeException("MEDORG. Не удалось извлечь restCallbackUrl из JMS-сообщения queue/RestCallbackQueue для id принятого сообщения ЕГИСЗ ИПС: " + egiszRespMessageId + ".", e);
        }

        return result;
    }
}
