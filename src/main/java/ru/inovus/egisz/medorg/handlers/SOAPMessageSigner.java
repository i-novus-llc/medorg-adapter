package ru.inovus.egisz.medorg.handlers;

import org.apache.xml.security.c14n.Canonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inovus.egisz.medorg.api.dto.SOAPMessageType;
import ru.inovus.egisz.medorg.datatypes.ConsumerRelate;
import ru.inovus.egisz.medorg.service.ConsumerRelateExtractor;
import ru.inovus.egisz.medorg.util.XmlHelper;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static ru.inovus.egisz.medorg.util.CryptoHelper.getBase64Digest;
import static ru.inovus.egisz.medorg.util.CryptoHelper.getSignature;

/**
 * Класс-обработчик для подписи SOAP-сообщений, отправляемых на сервисы ЕГИСЗ ИПС
 */
public class SOAPMessageSigner implements SOAPHandler<SOAPMessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(SOAPMessageSigner.class);

    private static final String WWW_W3_ORG_2000_09_XMLDSIG = "http://www.w3.org/2000/09/xmldsig#";
    private static final String WWW_W3_ORG_2001_10_XML_EXC_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";
    private static final String WWW_W3_ORG_2001_04_XMLDSIG_GOST34102001 = "http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411";
    private static final String WWW_W3_ORG_2001_04_XMLDSIG_GOSTR3411 = "http://www.w3.org/2001/04/xmldsig-more#gostr3411";
    private static final String WWW_W3_ORG_2005_08_ADDRESSING = "http://www.w3.org/2005/08/addressing";
    private static final String OASIS_200401_WSS_WSSECURITY_SECEXT = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String OASIS_200401_WSS_WSSECURITY_UTILITY = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String OASIS_200401_WSS_X509_TOKEN_PROFILE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";
    private static final String OASIS_200401_WSS_SOAP_MESSAGE_SECURITY = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";

    private static final String ROSMINZDRAV_NAMESPACE_URI = "http://egisz.rosminzdrav.ru";

    private String informationSystemName;
    private String pemCertificate;
    private String pemPrivateKey;

    public SOAPMessageSigner(){}

    public SOAPMessageSigner(String informationSystemName, String pemCertificate, String pemPrivateKey){
        this.informationSystemName = informationSystemName;
        this.pemCertificate = pemCertificate;
        this.pemPrivateKey = pemPrivateKey;
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        Boolean isOutbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        SOAPMessage message = context.getMessage();

        SOAPPart soapPart = message.getSOAPPart();

        try {

            SOAPEnvelope soapEnvelope = soapPart.getEnvelope();

            SOAPHeader soapHeader = soapEnvelope.getHeader();

            SOAPBody soapBody = soapEnvelope.getBody();

            SOAPMessageType messageType = SOAPMessageType.get(soapBody.getFirstChild().getNodeName());

            if(messageType == SOAPMessageType.SEND_RESPONSE) {

                SOAPElement idElem = (SOAPElement) XmlHelper.selectSingleNode(soapBody, "//*[local-name()='id']");

                ConsumerRelate consumerRelate = ConsumerRelateExtractor.extractInfo(idElem.getTextContent());

                informationSystemName = consumerRelate.getInformationSystemName();

                pemCertificate = consumerRelate.getPemCertificate();

                pemPrivateKey = consumerRelate.getPemPrivateKey();

                context.put("informationSystemName", informationSystemName);

                context.put("pemCertificate", pemCertificate);

                context.put("pemPrivateKey", pemPrivateKey);

                SOAPElement messageIDElem = (SOAPElement) XmlHelper.selectSingleNode(soapHeader, "//*[local-name()='MessageID']");

                context.put("MessageID", messageIDElem.getTextContent());
            }

            if (isOutbound != null && isOutbound) {

                soapEnvelope.setAttribute("xmlns:ds", WWW_W3_ORG_2000_09_XMLDSIG);
                soapEnvelope.setAttribute("xmlns:wsa", WWW_W3_ORG_2005_08_ADDRESSING);
                soapEnvelope.setAttribute("xmlns:wsse", OASIS_200401_WSS_WSSECURITY_SECEXT);
                soapEnvelope.setAttribute("xmlns:wsu", OASIS_200401_WSS_WSSECURITY_UTILITY);
                soapEnvelope.setAttribute("xmlns:egisz", ROSMINZDRAV_NAMESPACE_URI);
                soapBody.setAttribute("wsu:Id", "body");

                if(messageType == SOAPMessageType.SEND_RESPONSE_RESPONSE) {

                    informationSystemName = (String)context.get("informationSystemName");

                    pemCertificate = (String)context.get("pemCertificate");

                    pemPrivateKey = (String)context.get("pemPrivateKey");

                    String messageID = (String)context.get("MessageID");

                    SOAPElement relatesToElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2005_08_ADDRESSING, "RelatesTo", "wsa"));
                    relatesToElem.setTextContent(messageID);
                }

                /* добавление элемента для указания идентификатора сообщения */
                addingMessageIdElement(soapHeader);

                /* добавление элемента для указания идентификатора системы */
                addingClientEntityIdElement(soapHeader);

                /* добавление блока элементов для указания ЭП */
                addingSignatureElements(soapEnvelope, soapHeader);

                // Делаем такое преобразование, чтобы не поломался в последующем хэш для Body
                resetSOAPPartContent(message);

                /* проставление в элементе DigestValue расчитанную хеш-сумму блока с бизнес-данными запроса */
                SOAPElement digestValueElem = (SOAPElement) XmlHelper.selectSingleNode(message.getSOAPHeader(), "//*[local-name()='DigestValue']");
                genericDigestValue(message.getSOAPBody(), digestValueElem);

                /* подписание ЭП-ОВ */
                signDigestValue(message.getSOAPBody());
            }

            if (logger.isDebugEnabled()) {

                String debugMessage = isOutbound != null && isOutbound ? "Подготовлено к отправке в" : "Получено из";

                logger.debug("MEDORG. {} ЕГИСЗ ИПС {}: {}", debugMessage, messageType.getValue(), XmlHelper.elementToString(soapPart.getDocumentElement()));
            }

        } catch (SOAPException e) {

            String errDescription = isOutbound != null && isOutbound ? "подготовке сообщения к отправке в" : "получении сообщения из";

            logger.error("MEDORG. Ошибка при {} ЕГИСЗ ИПС.", errDescription, e);

            return false;
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {

        if (logger.isDebugEnabled()) {

            SOAPMessage message = context.getMessage();

            SOAPPart soapPart = message.getSOAPPart();

            Boolean isOutbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            String debugMessage = isOutbound != null && isOutbound ? "при отправке сообщения в" : "при получении сообщения из";

            logger.debug("MEDORG. Ошибка {} ЕГИСЗ ИПС: {}", debugMessage, XmlHelper.elementToString(soapPart.getDocumentElement()));
        }

        return true;
    }

    /**
     * Добавляет блок элементов для указания ЭП
     *
     * @param soapHeader
     * @throws SOAPException
     */
    private void addingSignatureElements(final SOAPEnvelope soapEnvelope, final SOAPHeader soapHeader) throws SOAPException {

        SOAPElement securityElem = soapHeader.addChildElement(new QName(OASIS_200401_WSS_WSSECURITY_SECEXT, "Security", "wsse"));

        SOAPElement binarySecurityTokenElem = soapHeader.addChildElement(new QName(OASIS_200401_WSS_WSSECURITY_SECEXT, "BinarySecurityToken", "wsse"));
        binarySecurityTokenElem.addAttribute(soapEnvelope.createName("EncodingType"), OASIS_200401_WSS_SOAP_MESSAGE_SECURITY);
        binarySecurityTokenElem.addAttribute(soapEnvelope.createName("ValueType"), OASIS_200401_WSS_X509_TOKEN_PROFILE);
        binarySecurityTokenElem.addAttribute(soapEnvelope.createName("Id", "wsu", OASIS_200401_WSS_WSSECURITY_UTILITY), "CertId");
        securityElem.addChildElement(binarySecurityTokenElem);
        binarySecurityTokenElem.setTextContent(pemCertificate);

        SOAPElement signatureElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "Signature", "ds"));

        SOAPElement signedInfoElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "SignedInfo", "ds"));

        SOAPElement canonicalizationMethodElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "CanonicalizationMethod", "ds"));
        canonicalizationMethodElem.setAttribute("Algorithm", WWW_W3_ORG_2001_10_XML_EXC_C14N);
        signedInfoElem.addChildElement(canonicalizationMethodElem);

        SOAPElement signatureMethodElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "SignatureMethod", "ds"));
        signatureMethodElem.setAttribute("Algorithm", WWW_W3_ORG_2001_04_XMLDSIG_GOST34102001);
        signedInfoElem.addChildElement(signatureMethodElem);

        SOAPElement referenceElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "Reference", "ds"));
        referenceElem.setAttribute("URI", "#body");

        SOAPElement transformsElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "Transforms", "ds"));

        SOAPElement transformElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "Transform", "ds"));
        transformElem.setAttribute("Algorithm", WWW_W3_ORG_2001_10_XML_EXC_C14N);
        transformsElem.addChildElement(transformElem);

        referenceElem.addChildElement(transformsElem);

        SOAPElement digestMethodElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "DigestMethod", "ds"));
        digestMethodElem.setAttribute("Algorithm", WWW_W3_ORG_2001_04_XMLDSIG_GOSTR3411);

        SOAPElement digestValueElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "DigestValue", "ds"));

        referenceElem.addChildElement(digestValueElem);
        referenceElem.addChildElement(digestMethodElem);

        signedInfoElem.addChildElement(referenceElem);

        signatureElem.addChildElement(signedInfoElem);

        SOAPElement signatureValueElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "SignatureValue", "ds"));
        signatureElem.addChildElement(signatureValueElem);

        SOAPElement keyInfoElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2000_09_XMLDSIG, "KeyInfo", "ds"));

        SOAPElement securityTokenReferenceElem = soapHeader.addChildElement(new QName(OASIS_200401_WSS_WSSECURITY_SECEXT, "SecurityTokenReference", "wsse"));

        SOAPElement referElem = soapHeader.addChildElement(new QName(OASIS_200401_WSS_WSSECURITY_SECEXT, "Reference", "wsse"));
        referElem.addAttribute(soapEnvelope.createName("URI"), "#CertId");
        referElem.addAttribute(soapEnvelope.createName("ValueType"), OASIS_200401_WSS_X509_TOKEN_PROFILE);

        securityTokenReferenceElem.addChildElement(referElem);

        keyInfoElem.addChildElement(securityTokenReferenceElem);

        signatureElem.addChildElement(keyInfoElem);

        securityElem.addChildElement(signatureElem);
    }

    /**
     * Добавляет элемент для указания идентификатора системы
     *
     * @param soapHeader
     * @throws RuntimeException
     */
    private void addingClientEntityIdElement(final SOAPHeader soapHeader) {

        try {

            SOAPElement transportHeaderElement = soapHeader.addChildElement(new QName(ROSMINZDRAV_NAMESPACE_URI, "transportHeader", "egisz"));

            SOAPElement authInfoElement = soapHeader.addChildElement(new QName(ROSMINZDRAV_NAMESPACE_URI, "authInfo", "egisz"));

            SOAPElement clientEntityIdElement = soapHeader.addChildElement(new QName(ROSMINZDRAV_NAMESPACE_URI, "clientEntityId", "egisz"));

            clientEntityIdElement.setTextContent(informationSystemName);

            authInfoElement.addChildElement(clientEntityIdElement);

            transportHeaderElement.addChildElement(authInfoElement);
        } catch (SOAPException ex) {
            throw new RuntimeException("Не удалось добавить элемент для указания идентификатора системы.", ex);
        }
    }

    /**
     * Проставляет в элемент расчитанную хеш-сумму для значения заданного узла
     *
     * @param node            обрабатываемый узел
     * @param destinationElem элмент, в который проставляется значение хеш-суммы
     * @throws RuntimeException
     */
    private void genericDigestValue(final org.w3c.dom.Node node, final SOAPElement destinationElem) {

        try {

            byte[] subtreeCanonicalized = XmlHelper.getCanonicalizeSubtree(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, node);

            destinationElem.addTextNode(getBase64Digest(new String(subtreeCanonicalized)));

        } catch (SOAPException ex) {
            throw new RuntimeException("Не удалось вычислить хеш-сумму для блока элементов.", ex);
        }
    }

    private void resetSOAPPartContent(final SOAPMessage message) {

        ByteArrayOutputStream rawMessage = new ByteArrayOutputStream();

        try {
            message.writeTo(rawMessage);
            message.getSOAPPart().setContent(new StreamSource(new ByteArrayInputStream(rawMessage.toByteArray())));
        } catch (SOAPException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cчитывает подпись после всех манипуляций с SignedInfo
     *
     * @param soapBody объект содержания тела SOAP-сообщения
     * @throws RuntimeException
     */
    private void signDigestValue(final SOAPBody soapBody) {

        try {

            SOAPElement signatureValueElem = (SOAPElement) XmlHelper.selectSingleNode(soapBody, "//*[local-name()='SignatureValue']");

            SOAPElement signedInfoElem = (SOAPElement) XmlHelper.selectSingleNode(soapBody, "//*[local-name()='SignedInfo']");

            byte[] subtreeCanonicalized = XmlHelper.getCanonicalizeSubtree(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, signedInfoElem);

            signatureValueElem.addTextNode(getSignature(new String(subtreeCanonicalized), pemPrivateKey));

        } catch (GeneralSecurityException | SOAPException ex) {
            throw new RuntimeException("Не удалось считать подпись после всех манипуляций с SignedInfo.", ex);
        }
    }

    /**
     * Добавляет элемент для указания идентификатора сообщения
     *
     * @param soapHeader
     * @throws SOAPException
     */
    private void addingMessageIdElement(final SOAPHeader soapHeader) throws SOAPException {
        SOAPElement messageIdElem = soapHeader.addChildElement(new QName(WWW_W3_ORG_2005_08_ADDRESSING, "MessageID", "wsa"));
        messageIdElem.setTextContent(UUID.randomUUID().toString());
    }

    @Override
    public void close(MessageContext context) {}
}
