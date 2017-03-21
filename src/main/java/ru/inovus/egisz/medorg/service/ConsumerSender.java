package ru.inovus.egisz.medorg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inovus.egisz.medorg.callback.ResponseCommand;
import ru.inovus.egisz.medorg.exceptions.ConsumerHttpBadGatewayException;
import ru.inovus.egisz.medorg.exceptions.NotDeliveredException;
import ru.inovus.egisz.medorg.rest.RestCallbackCommand;
import ru.inovus.egisz.medorg.rest.XmlResultContent;
import ru.inovus.egisz.medorg.util.HttpRequester;
import ru.inovus.egisz.medorg.util.XmlHelper;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.IOException;
import java.net.HttpURLConnection;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/jms/queue/RestCallbackQueue")})
public class ConsumerSender implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerSender.class);

    @EJB
    private MessageInfoService messageInfoService;

    @Resource
    private MessageDrivenContext messageDrivenContext;

    @Override
    public void onMessage(Message message) {
        ResponseCommand response = null;

        try {
            response = message.getBody(ResponseCommand.class);
        } catch (JMSException e) {
            logger.error("MEDORG. Не удается разобрать ответ из JMS-очереди", e);
        }

        if (response == null)
            return;

        final String id = response.getMessageId();
        final RestCallbackCommand command = messageInfoService.getConsumerByMessageId(id);

        if (command != null) {

            final String restCallbackUrl = command.getCallbackUrl();

            final String authorizedUserName = command.getAuthorizedUserName();

            logger.debug("MEDORG. Извлечена привязка restCallbackUrl {} к id принятого сообщения {} для потребителя {}", restCallbackUrl, id, authorizedUserName);

            final String data = getResultData(id, response.getOid(), response.getResponse());

            logger.debug("MEDORG. Подготовлено к отправке на сервис {} потребителя {} результирующее сообщение: {}", restCallbackUrl, authorizedUserName, data);

            if (data != null) {
                try {
                    responseDelivering(restCallbackUrl, authorizedUserName, data, id);

                    logger.debug("MEDORG. Доставлено на сервис {} потребителя {} результирующее сообщение: {}.", restCallbackUrl, authorizedUserName, data);

                    messageInfoService.deleteMessage(id);

                    logger.debug("MEDORG. Удалена привязка restCallbackUrl {} к id принятого сообщения {}", restCallbackUrl, id);

                } catch (RuntimeException e) {
                    logger.warn("MEDORG. Не удалось отправить клиенту сообщение с id {}. Будет предпринята попытка обработать его позднее", id);
                    messageDrivenContext.setRollbackOnly();
                }
            }
        } else {
            logger.warn("MEDORG. Не найдена информация по сообщению с id {}. Будет предпринята попытка обработать его позднее", id);
            messageDrivenContext.setRollbackOnly();
        }
    }

    /**
     * Доставка результирующего сообщения ЕГИСЗ ИПС на сервис потребителя
     *
     * @param restCallbackUrl    url - для отправки результата обработки
     * @param authorizedUserName логин авторизированного пользователя
     * @param data               строка результирующего сообщения подготовленного к отправке на сервис потребителя
     * @param id
     * @throws ConsumerHttpBadGatewayException
     */
    private void responseDelivering(final String restCallbackUrl, final String authorizedUserName, final String data, final String id) {

        int responseCode;

        try {

            responseCode = HttpRequester.post(restCallbackUrl, data);

        } catch (IOException ex) {
            throw new NotDeliveredException(ex);
        }

        if (responseCode == HttpURLConnection.HTTP_BAD_GATEWAY) {

            throw new ConsumerHttpBadGatewayException("MEDORG. В связи с проблемой работы сервиса " + restCallbackUrl + " (HTTP-статус 502 Bad Gateway) потребителя " + authorizedUserName + ", не удалось доставить результирующее сообщение ЕГИСЗ ИПС: " + data);

        } else if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_ACCEPTED) {

            throw new NotDeliveredException("HTTP-статус полученного сообщения от потребителя: " + responseCode);
        }
    }

    /**
     * Возвращает тело запроса потребителю результирующего сообщения ЕГИСЗ
     *
     * @param egiszRespMessageId id принятого сообщения ЕГИСЗ
     * @param oid                идентификатор базового объекта
     * @param response           документ, кодированный в base64, который содержит результат обработки сообщения
     * @return
     */
    private String getResultData(final String egiszRespMessageId, String oid, String response) {

        String result = null;

        final XmlResultContent content = new XmlResultContent(egiszRespMessageId, oid, response);

        try {

            result = XmlHelper.instanceToString(content, XmlResultContent.class);

        } catch (Exception ex) {
            logger.error("MEDORG. Не удалось преобразовать строку response в объект XmlResultContent для id принятого сообщения ЕГИСЗ ИПС {}: oid={}, response={}", egiszRespMessageId, oid, response, ex);
        }

        return result;
    }
}
