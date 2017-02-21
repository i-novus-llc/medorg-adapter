package ru.inovus.egisz.medorg.util;

import org.apache.commons.lang3.CharEncoding;

import javax.ws.rs.core.MediaType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Обеспечивает отправку запросов по протоколу HTTP/HTTPS
 */
public class HttpRequester {

    /**
     * Отправляет POST-запрос
     * @throws IOException
     */
    public static int post(final String urlPath, final String params) throws IOException {

        HttpURLConnection con = (HttpURLConnection) new URL(urlPath).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Content-Type", MediaType.APPLICATION_XML);
        con.setRequestProperty("Charset", CharEncoding.UTF_8);
        con.setDoOutput(true);

        DataOutputStream wr = null;

        try {

            wr = new DataOutputStream(con.getOutputStream());

            wr.writeBytes(params);
            wr.flush();

            return con.getResponseCode();

        } finally {

            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException e) {
                    /* NOP */
                }
            }
        }
    }
}
