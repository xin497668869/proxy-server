package com.xin.test.proxy.https;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class SslServer {
    public static void main(String[] args) throws Exception {
        SSLContext context = SSLContext.getInstance("TLSv1.2");

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream("C:\\Users\\xin\\.keystore"), "12345678".toCharArray());
        Certificate certificate = keyStore.getCertificate("baidu");

        // 初始化key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                                                                      .getDefaultAlgorithm());
        kmf.init(keyStore, "12345678".toCharArray());

        context.init(kmf.getKeyManagers(),
                     null,
                     null);

        SSLServerSocket serverSocket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket(8099);
        serverSocket.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
//        serverSocket.setNeedClientAuth(true);
        System.out.println("准备accept");
        while (true) {
            try {
                Socket accept = serverSocket.accept();
                System.out.println("acc");
                byte[] bytes = new byte[2048];


                byte[] htmlbuffer = new byte[2048];
                int len;
                while ((len = accept.getInputStream().read(htmlbuffer)) != -1) {
                    String x = new String(htmlbuffer, 0, len);
                    System.out.println(x);
                    if (x.contains("\r\n\r\n")) {
                        break;
                    }
                }

                OutputStream outputStream = accept.getOutputStream();
                byte[] b = ("HTTP/1.1 200 \r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Transfer-Encoding: chunked\r\n" +
                        "Date: Tue, 15 May 2018 16:52:32 GMT\r\n" +
                        "\r\n" +
                        "1f\r\n" +
                        "{\"statusCode\":401,\"message\":\"\"}\r\n" +
                        "0\r\n" +
                        "\r\n").getBytes();
                outputStream.write(b);
                System.out.println(new String(b));
                outputStream.flush();
                accept.close();
                System.out.println("socket关闭");
            } catch (Exception E) {
                E.printStackTrace();
            }
        }
//        int read = accept.getInputStream().read(bytes);
//        System.out.println("accept " + accept + "   " + new String(bytes, 0, read));
//        accept.close();
    }
}
