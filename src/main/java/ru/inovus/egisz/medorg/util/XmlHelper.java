package ru.inovus.egisz.medorg.util;

import org.apache.commons.lang3.CharEncoding;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Утилита для работы с XML
 */
public class XmlHelper {

    /**
     * Возвращает преобразованный XML-элемент из строки
     * @param element объект элемента для обработки
     * @param xmlEncoding кодировка, которая задаётся в объявлении XML
     * @return
     * @throws RuntimeException
     */
    public static String elementToString(final Element element, final Charset xmlEncoding) {

        StringWriter writer = null;
        Transformer tf = null;

        try {
            tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.ENCODING, xmlEncoding.name());

            writer = new StringWriter();

            tf.transform(new DOMSource(element), new StreamResult(writer));

        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    /**
     * Возвращает преобразованный XML-элемент из строки
     * @param element объект элемента для обработки
     * @return
     * @throws RuntimeException
     */
    public static String elementToString(final Element element) {
        return elementToString(element, Charset.forName(CharEncoding.UTF_8));
    }


    /**
     * Возвращает найденный объект SOAPElement-элемента из заданного узла
     * @param contextNode узел
     * @param exprString условия поиска
     * @return
     * @throws RuntimeException
     */
    public static org.w3c.dom.Node selectSingleNode(final org.w3c.dom.Node contextNode, final String exprString){

        org.w3c.dom.Node result;

        try {
            result =  XPathAPI.selectSingleNode(contextNode, exprString);
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }

    /**
     * Возвращает массив байтов канонизированного блока для заданного узла
     * @return
     * @throws RuntimeException
     */
    public static byte[] getCanonicalizeSubtree(final String algorithmURI, final org.w3c.dom.Node node){

        byte[] result;

        try {
            result = Canonicalizer.getInstance(algorithmURI).canonicalizeSubtree(node);
        } catch (CanonicalizationException | InvalidCanonicalizerException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }


    public static String instanceToString(final Object object, Class... classesToBeBound) {

        String result = null;

        JAXBContext context = null;

        try {

            context = JAXBContext.newInstance(classesToBeBound);

            Marshaller marshaller = null;

            marshaller = context.createMarshaller();

            StringWriter sw = new StringWriter();

            marshaller.marshal(object, sw);
            result = sw.toString();

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
