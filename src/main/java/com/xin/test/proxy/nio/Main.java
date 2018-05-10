package com.xin.test.proxy.nio;

import com.xin.test.proxy.RequestDto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Main {

    public static SocketBinds socketBinds = new SocketBinds();

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(8093));
        while (true) {
//            System.out.println("等待伦旭中=== ");
            int select = selector.select();
            System.out.println("等待伦旭中===2  " + selector.keys().size());
            if (select < 1) {
                continue;
            }
            System.out.println("轮训中..");
            Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
            while (selectionKeys.hasNext()) {
                SelectionKey selectionKey = selectionKeys.next();
                if (!selectionKey.isValid()) {
                    cancel(selectionKey, (SocketChannel) selectionKey.channel());
                    selectionKeys.remove();
                    continue;
                }
                if (selectionKey.isAcceptable()) {
                    System.out.println("isAcceptable");
                    SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ, true);
                    System.out.println(System.currentTimeMillis() + "=====  accept " + socketChannel);
                }
                if (selectionKey.isConnectable()) {
                    System.out.println("isConnectable");
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    if (channel.isConnectionPending()) {
                        if (channel.finishConnect()) {
                            //只有当连接成功后才能注册OP_READ事件
                            System.out.println("准备连接 " + channel);
                            selectionKey.interestOps(SelectionKey.OP_READ);
                            RequestDto requestDto = socketBinds.findRequestByEach(channel);
                            if (requestDto.getRequestLine() != null) {
                                int write = channel.write(ByteBuffer.wrap(requestDto.getRequestLine().getBytes()));
                                System.out.println("暂时写进去  " + write + "   " + requestDto.getRequestLine());
                            }
                        } else {
                            cancel(selectionKey, channel);
                        }
                    }
                }
                if (selectionKey.isReadable()) {
                    System.out.println("isReadable");
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    System.out.println(System.currentTimeMillis() + "=====  isReadable " + channel);
                    System.out.println(selectionKey.isReadable());
                    if (selectionKey.attachment().equals(true)) {
                        RequestDto requestDto = getRequestDto(channel);
                        if (requestDto != null) {
                            SocketChannel socketChannel = SocketChannel.open();
                            socketChannel.configureBlocking(false);
                            System.out.println(requestDto);
                            try {
                                socketChannel.connect(new InetSocketAddress(requestDto.getConnectHost(), requestDto.getConnectPort()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.err.println("无法连接, 拒绝");
                            }
                            System.out.println(socketChannel + " 准备连接 " + socketChannel.isConnected());
                            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT, false);

                            System.out.println("register " + socketChannel);
                            System.out.println(requestDto);
                            selectionKey.attach(false);
                            socketBinds.put(channel, socketChannel, requestDto);
                        } else {
                            cancel(selectionKey, channel);
                        }
                    } else {
                        SocketChannel channel1 = socketBinds.findByEach(channel);
                        if (channel1 == null) {
                            if (socketBinds.findRequestByEach(channel) != null) {
                                System.out.println("cancel " + socketBinds.findRequestByEach(channel).getRequestLine());
                            }

                        } else {
                            if (channel1.isConnected()) {
                                if (!readToWrite(channel, channel1)) {
                                    //如果是第一次就挂了
                                    cancel(selectionKey, channel);
                                }
                            } else {
                                System.out.println(channel1 + " 还没准备好" + channel1.isConnected());
                                Thread.sleep(100);
                            }
                        }

                    }

                }
                selectionKeys.remove();
            }
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

    private static boolean readToWrite(SocketChannel channel, SocketChannel channel1) {
        boolean isFirst = true;
        try {
            while (true) {
                ByteBuffer allocate = ByteBuffer.allocate(20048);
                int read = channel.read(allocate);

                if (read < 0) {
                    System.out.println(read + "  read " + channel.read(allocate));
                    return !isFirst;
                }
                if (read == 0) {
                    return !isFirst;
                }
                int total = 0;
                allocate.flip();

                while (allocate.hasRemaining()) {
                    channel1.write(allocate);
                    System.out.println("轮训中.......");
                }
                System.out.println(read + "  " + total + "  " + allocate.limit());
                isFirst = false;
                System.out.println("=====  read  " + socketBinds.findRequestByEach(channel).getUrl());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static RequestDto getRequestDto(SocketChannel channel) throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate(1000);
        String content = "";
        while (content.indexOf("\r\n\r\n") < 0) {
            ByteBuffer eachByte = ByteBuffer.allocate(1);
            int read = channel.read(eachByte);
            allocate.put(eachByte.array(), eachByte.arrayOffset(), eachByte.position());
            content = new String(allocate.array(), allocate.arrayOffset(), allocate.position());
            if (content.contains("\r\n\r\n")) {
                RequestDto requestDto = new RequestDto(content);
                System.out.println(System.currentTimeMillis() + "=====  content  " + requestDto.getUrl());
                return requestDto;
            }
            if (read == -1) {
                return null;
            }
        }
        return null;
    }


}
