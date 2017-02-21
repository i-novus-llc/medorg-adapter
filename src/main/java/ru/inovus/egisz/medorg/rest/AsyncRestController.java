package ru.inovus.egisz.medorg.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inovus.egisz.medorg.service.MessageExchangeBean;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Обеспечивает обмен сообщениями между ИС потребителя и ЕГИСЗ ИПС через REST
 */
@Path("/")
public class AsyncRestController {

    private static final Logger logger = LoggerFactory.getLogger(AsyncRestController.class);

    @EJB
    private MessageExchangeBean messageExchangeBean;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Принимает Rest-запрос от ИС потребителя для отправки сообщения в ЕГИСЗ
     *
     * @param asyncResponse    объект, который позволяет асинхронно отправлять ответ в другом потоке
     * @param securityContext  security-контекст для jax-rs
     * @param oid              идентификатор базового объекта. Используется для авторизации клиента, зависит от целевой подсистемы. Например, для ФРМО - это OID медицинского учреждения.
     * @param service          имя подсистемы и целевого метода. Указывается в формате target.method, где target - имя подсистемы, method - имя метода подсистемы.
     * @param base64Document   строка XML-документа перекодированная в Base64
     * @param restCallbackUrl  url - для отправки результата обработки
     */
    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/sendDocument")
    public void sendDocument(final @Suspended AsyncResponse asyncResponse, final @Context SecurityContext securityContext, final @QueryParam("endPoint") String ipsEndPoint, @FormParam("oid") String oid, @FormParam("service") String service, @FormParam("document") String base64Document , @FormParam("callbackUrl") String restCallbackUrl) {

        final String authorizedUserName = securityContext.getUserPrincipal().getName();

        logger.debug("MEDORG. Услышан sendDocument-запрос от потребителя {}: endPoint={}, oid={}, service={}, document={}, callbackUrl={}", authorizedUserName, ipsEndPoint, oid, service, base64Document, restCallbackUrl);

        asyncResponse.setTimeout(3, TimeUnit.MINUTES);

        executorService.submit(() -> {
            /* отправка SendDocument-запроса в ЕГИСЗ */
            messageExchangeBean.sendDocument(asyncResponse, authorizedUserName, ipsEndPoint, oid, service, base64Document, restCallbackUrl);
        });
    }

}
