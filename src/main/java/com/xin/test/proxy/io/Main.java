package com.xin.test.proxy.io;

import com.xin.test.proxy.RequestDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Main {

    public static final String DELIVER = "\r\n\r\n";

    public static String notSupportResponse() {
        String body = "HTTP/1.1 404 \r\n" +
                "Server: openresty/1.13.6.1\r\n" +
                "Date: Thu, 10 May 2018 02:20:10 GMT\r\n" +
                "Content-Type: application/json;charset=UTF-8\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Connection: keep-alive\r\n" +
                "\r\n" +
                "27\r\n" +
                "{\"code\":\"070111\",\"message\":\"Not Found\"}\r\n" +
                "0\r\n" +
                "\r\n";
        return body;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8093);
        while (true) {
            final Socket proxySocket = serverSocket.accept();
            new Thread(() -> {
                try {
                    RequestDto requestDto = getRequestDto(proxySocket.getInputStream());
                    System.out.println("requestDto " + requestDto);

                    if (requestDto.getMethod().equals("CONNECT")) {
                        requestDto.setRequestLine(null);
                        System.out.println("简历连接");
                        proxySocket.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                    }

                    Socket destSocket = new Socket();
                    destSocket.connect(new InetSocketAddress(requestDto.getConnectHost(), requestDto.getConnectPort()));
                    if (requestDto.getRequestLine() != null) {
                        byte[] bytes = requestDto.getRequestLine().getBytes();
                        System.out.println(bytes.length + "  lengtttt  " + requestDto.getRequestLine());
                        destSocket.getOutputStream().write(bytes);
                    }
                    new Thread(() -> readToWrite(destSocket, proxySocket)).start();

                    readToWrite(proxySocket, destSocket);

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }).start();

        }
    }

    private static void readToWrite(Socket destSocket, Socket proxySocket) {

        while (true) {
            try {
                proxySocket.getOutputStream().write(destSocket.getInputStream().read());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(proxySocket + "  " + destSocket);
                closeAll(proxySocket, destSocket);
                return;
            }
        }

    }

    private static void closeAll(Socket proxySocket, Socket destSocket) {
        try {
            proxySocket.close();
            destSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static RequestDto getRequestDto(InputStream inputStream) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10000);
        while (true) {
            byteBuffer.put((byte) inputStream.read());
            String header = new String(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.position());
            if (header.contains(DELIVER)) {
                return new RequestDto(header);
            }
        }
    }


}
