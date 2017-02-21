package ru.inovus.egisz.medorg.util;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.util.List;

/**
 *  Надстройщик сервиса обмена SOAP-сообщениями
 */
public class SOAPConfigurator {

    /**
     * Настраивает Apache CXF, чтобы в заголовках сообщений вычислялся Content-Length
     *
     * @param port объект порта точки доступа в СМЭВ
     */
    public static void сontentLengthActivate(final Object port) {

        HTTPClientPolicy hTTPClientPolicy = new HTTPClientPolicy();
        hTTPClientPolicy.setAllowChunking(false);
        Client client = org.apache.cxf.jaxws.JaxWsClientProxy.getClient(port);
        HTTPConduit http = (HTTPConduit) client.getConduit();
        http.setClient(hTTPClientPolicy);
    }

    /**
     * Подключает SOAPHandler
     *
     * @param port объект порта точки доступа в СМЭВ
     * @param soapHandler SOAPHandler
     * @param isMTOMEnabled признак активации/деактивации MTOM
     */
    public static void bindingSOAPHandler(final Object port, final SOAPHandler<SOAPMessageContext> soapHandler, final boolean isMTOMEnabled) {

        BindingProvider provider = (BindingProvider) port;
        Binding binding = provider.getBinding();
        ((SOAPBinding) binding).setMTOMEnabled(isMTOMEnabled);

        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add(soapHandler);
        binding.setHandlerChain(handlerChain);
    }

    /**
     * Настраивает url - точки доступа к серверу СМЭВ
     * @param port
     * @param endpointURL
     */
    public static void setEndpointURL(final Object port, final String endpointURL){
        BindingProvider bp = (BindingProvider)port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
    }
}
