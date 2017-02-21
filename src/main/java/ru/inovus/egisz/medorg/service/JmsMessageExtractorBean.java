package ru.inovus.egisz.medorg.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.*;
import java.util.Enumeration;

/**
 * Обеспечивает извлечение JMS-сообщения из очереди
 */
@Stateless
public class JmsMessageExtractorBean {

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    @JMSSessionMode(JMSContext.DUPS_OK_ACKNOWLEDGE)
    private JMSContext jmsContext;

    /**
     * Возвращает извлечённое JMS-сообщение из заданной очереди
     *
     * @param jmsCorrelationID идентификатор сообщения
     * @param queue            очередь
     * @return
     * @throws RuntimeException
     */
    public Message getMessage(final String jmsCorrelationID, final Queue queue) {

        Message result = null;

        QueueBrowser browser = jmsContext.createBrowser(queue, "JMSCorrelationID='"+ jmsCorrelationID +"'");

        try {

            Enumeration enumeration = browser.getEnumeration();

            while (enumeration.hasMoreElements()) {
                result = (Message) enumeration.nextElement();
            }

        } catch (JMSException e) {

            throw new RuntimeException("MEDORG. Ошибка при получении jms-сообщения из очереди посредством QueueBrowser-а.", e);

        } finally {

            if (browser != null) {

                try {
                    browser.close();
                } catch (JMSException e) {
                    /* NOP */
                }
            }
        }

        return result;
    }
}
