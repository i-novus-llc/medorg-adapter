package ru.inovus.egisz.medorg.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Утилита для работы с исключениями
 */
public class ExceptionHelper {

    /**
     * Возвращает текст сообщения исключения, при отсутствии возвращается stackTrace исключения
     * @param throwable объект исключения
     * @return
     */
    public static String getMessageOrStackTrace(Throwable throwable) {

        Throwable cause = ExceptionUtils.getRootCause(throwable);

        if(cause == null){
            return throwable.getMessage() == null ? ExceptionUtils.getStackTrace(throwable) : ExceptionUtils.getMessage(throwable);
        }

        return cause.getMessage() == null ? ExceptionUtils.getStackTrace(cause) : ExceptionUtils.getMessage(cause);
    }
}
