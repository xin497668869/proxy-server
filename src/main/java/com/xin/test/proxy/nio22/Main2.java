package com.xin.test.proxy.nio22;

import com.xin.test.proxy.nio.SocketBinds;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Main2 {

    public static final int           POLL_SIZE   = 1;
    public static       SocketBinds   socketBinds = new SocketBinds();
    public static       AtomicInteger incress     = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        System.out.println("准备绑定");
        serverSocketChannel.bind(new InetSocketAddress(8093));
        Poll[] polls = new Poll[POLL_SIZE];
        for (int i = 0; i < POLL_SIZE; i++) {
            polls[i] = new Poll();
            new Thread(polls[i]).start();
        }

        for (int i = 0; i < 1; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {

                            SocketChannel socketChannel = serverSocketChannel.accept();
                            System.out.println("有新连接 " + socketChannel);
                            polls[incress.incrementAndGet() % POLL_SIZE].register(socketChannel);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

    }

    private static void cancel(SelectionKey selectionKey, SocketChannel channel) throws IOException {
        if (selectionKey != null) {
            selectionKey.cancel();
        }
        if (socketBinds.findRequestByEach(channel) != null)
            System.out.println("cancel " + socketBinds.findRequestByEach(channel).getRequestLine());
        socketBinds.remove(channel);

    }


}
