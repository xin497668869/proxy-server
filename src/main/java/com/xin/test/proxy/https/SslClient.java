package com.xin.test.proxy.https;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class SslClient {

    public static String getContent() {
        return "GET / HTTP/1.1\r\n" +
                "Host: www.smzdm.com\r\n" +
                "Cache-Control: no-cache\r\n" +
                "\r\n";
    }

    public static void main(String[] args) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
//        KeyStore.getInstance("dks");
        Security.addProvider(new sun.security.mscapi.SunMSCAPI());
        KeyStore keystore = KeyStore.getInstance("Windows-ROOT");
        keystore.load(null, null);
        // 初始化key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                                                                      .getDefaultAlgorithm());
        kmf.init(keystore, null);

        SSLContext context = SSLContext.getInstance("TLSv1.2");


        TrustManagerFactory var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        var4.init(keystore);
        TrustManager[] trustManagers = var4.getTrustManagers();

        context.init(kmf.getKeyManagers(),
                     trustManagers,
                     null);

        Socket socket = context.getSocketFactory().createSocket("localhost", 8099);
        socket.getOutputStream().write(getContent().getBytes());
        socket.close();

    }
}
