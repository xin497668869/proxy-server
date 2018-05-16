package com.xin.test.proxy.https;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Main {

    public static String getContent() {
        return "GET / HTTP/1.1\r\n" +
                "Host: www.smzdm.com\r\n" +
                "Cache-Control: no-cache\r\n" +
                "asdfasdf" +
                "\r\n\r\n";
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static void main(String[] args) throws IOException {
        Socket socket = SSLSocketFactory.getDefault().createSocket("www.smzdm.com", 443);
//        Socket socket = SSLSocketFactory.getDefault().createSocket("113.200.91.245", 443);
//        Socket socket = new Socket("www.smzdm.com", 443);
        socket.getOutputStream().write(getContent().getBytes());
//        socket.getOutputStream().write(getContent().getBytes());
        byte[] bytes = new byte[2048];
        String content = "";
        while (socket.getInputStream().read(bytes) > 0) {
            content = content + new String(bytes);
            System.out.println(content);
        }
        System.out.println(content);
        socket.close();

    }
}
