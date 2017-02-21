package ru.inovus.egisz.medorg.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.TimeUnit;

/**
 * Обеспечивает добавление в очередь привязки restCallbackUrl к id принятого сообщения ЕГИСЗ ИПС
 */
@Stateless
public class RestCallbackRegistrator {

    private static final Logger logger = LoggerFactory.getLogger(RestCallbackRegistrator.class);

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    @JMSSessionMode(JMSContext.DUPS_OK_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Resource(lookup = "java:jboss/exported/jms/queue/RestCallbackQueue")
    private Destination restCallbackQueue;

    /**
     * Добавляет в очередь привязку restCallbackUrl к id принятого сообщения ЕГИСЗ ИПС
     *
     * @param egiszRespMessageId идентификатор принятого сообщения ЕГИСЗ ИПС
     * @param authorizedUserName логин авторизированного пользователя
     * @param restCallbackUrl    url - для отправки результата обработки
     */
    public void add(final String egiszRespMessageId, final String authorizedUserName, final String restCallbackUrl) {

        jmsContext.createProducer()
                .setTimeToLive(TimeUnit.DAYS.toMillis(7))
                .setJMSCorrelationID(egiszRespMessageId)
                .setAsync(new CompletionListener() {

                    @Override
                    public void onCompletion(Message message) {
                        logger.debug("MEDORG. Добавлено в очередь queue/RestCallbackQueue привязка restCallbackUrl '{}' потребителя {} к id принятого сообщения ЕГИСЗ ИПС {}.", restCallbackUrl, authorizedUserName, egiszRespMessageId);
                    }

                    @Override
                    public void onException(Message message, Exception exception) {
                        logger.error("MEDORG. Ошибка при добавлении в очередь queue/RestCallbackQueue привязки restCallbackUrl '{}' потребителя {} к id принятого сообщения ЕГИСЗ ИПС {}.", restCallbackUrl, authorizedUserName, egiszRespMessageId, exception);
                    }

                }).send(restCallbackQueue, new RestCallbackCommand(authorizedUserName, restCallbackUrl));
    }

}
