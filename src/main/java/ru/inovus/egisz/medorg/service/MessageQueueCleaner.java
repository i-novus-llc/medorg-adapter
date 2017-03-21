package ru.inovus.egisz.medorg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.util.Calendar;

@Singleton
@LocalBean
public class MessageQueueCleaner {
    private static final Logger logger = LoggerFactory.getLogger(MessageQueueCleaner.class);

    @EJB
    private MessageInfoService messageInfoService;

    @Schedule(minute="*/15", hour="*")
    public void execute(Timer timer) {

        logger.info("MEDORG. Очистка старых сообщений началась");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);

        int result = messageInfoService.removeOldMessages(calendar.getTime());
        logger.info("MEDORG. Очистка сообщений старше {} успешно завершена. Удалено {} сообщений", calendar.getTime(), result);
    }
}
