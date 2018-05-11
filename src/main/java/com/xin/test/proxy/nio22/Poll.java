package com.xin.test.proxy.nio22;

import com.xin.test.proxy.RequestDto;
import com.xin.test.proxy.nio.SocketBinds;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Poll implements Runnable {

    private Selector      selector;
    private AtomicBoolean isWakeup    = new AtomicBoolean(false);
    private SocketBinds   socketBinds = new SocketBinds();

    private BlockingQueue<Event> events = new ArrayBlockingQueue<>(100);

    public void register(SocketChannel socketChannel) throws InterruptedException {
        Event event = new Event();
        event.setEventType(Event.EventTypeEnum.REGISTER);
        event.setSocketChannel(socketChannel);
        events.put(event);
        isWakeup.compareAndSet(false, true);
        selector.wakeup();
    }

    public Poll() throws IOException {
        selector = Selector.open();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        while (true) {
            try {
                int selectSize;
                if (isWakeup.get()) {
                    selectSize = selector.selectNow();
                    isWakeup.set(false);
                } else {
                    selectSize = selector.selectNow();
                }

                if (selectSize > 0) {

                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    while (selectionKeys.hasNext()) {
                        SelectionKey selectionKey = selectionKeys.next();
                        SocketWrapper socketWrapper = (SocketWrapper) selectionKey.attachment();
//                        processKey(socketWrapper, selectionKey);
                        unreg(selectionKey, SelectionKey.OP_READ);
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    processKey(socketWrapper, selectionKey);
                                    if (selectionKey.isValid()) {
                                        reg(selectionKey, SelectionKey.OP_READ);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });


                        selectionKeys.remove();
                    }

                } else if (events.size() > 0) {
                    Event event = events.poll();
                    switch (event.getEventType()) {
                        case REGISTER:
                            event.getSocketChannel().configureBlocking(false);
                            event.getSocketChannel().register(selector, SelectionKey.OP_READ, new SocketWrapper(event.getSocketChannel(), this, false));
                            break;
                        case READ:
                            event.getSocketChannel().register(selector, SelectionKey.OP_READ);
                            break;
                        case WRITE:
                            event.getSocketChannel().register(selector, SelectionKey.OP_WRITE);
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void processKey(SocketWrapper socketWrapper, SelectionKey selectionKey) throws IOException {
        if (!selectionKey.isValid()) {
            selectionKey.cancel();
            return;
        }
        if (selectionKey.isReadable() && selectionKey.isValid()) {
            readEvent(socketWrapper, selectionKey);
        }
        if (selectionKey.isValid() && selectionKey.isConnectable()) {
            connectionEvent(selectionKey);
        }
    }

    private void connectionEvent(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        System.out.println("isConnectable  " + channel);
        if (channel.isConnectionPending()) {
            if (channel.finishConnect()) {
                //只有当连接成功后才能注册OP_READ事件
                System.out.println("准备连接 " + channel);
                selectionKey.interestOps(SelectionKey.OP_READ);
                RequestDto requestDto = socketBinds.findRequestByEach(channel);
                if (requestDto == null) {
                    cancel(selectionKey, channel);
                    return;
                }
                if (requestDto.getRequestLine() != null) {
                    int write = channel.write(ByteBuffer.wrap(requestDto.getRequestLine().getBytes()));
                    System.out.println("暂时写进去  " + write + "   " + requestDto.getRequestLine());
                }
            } else {
                cancel(selectionKey, channel);
            }
        }
    }

    private void readEvent(SocketWrapper socketWrapper, SelectionKey selectionKey) throws IOException {

        System.out.println(Thread.currentThread().getName() + " 收到时间  " + socketWrapper.isReadHeader() + "  " + socketWrapper.getSocketChannel());
        SocketChannel socketChannel = socketWrapper.getSocketChannel();
        if (!socketWrapper.isReadHeader()) {
            RequestDto requestDto = getRequestDto(socketChannel);
            if (requestDto == null) {
                cancel(selectionKey, socketChannel);
                return;
            }
            if (requestDto.getMethod().equals("CONNECT")) {
                requestDto.setRequestLine(null);
                System.out.println("简历连接");
                socketChannel.write(ByteBuffer.wrap("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes()));
            }

            SocketChannel destChannel = SocketChannel.open();
            destChannel.configureBlocking(false);
            System.out.println(requestDto);
            try {
                destChannel.connect(new InetSocketAddress(requestDto.getConnectHost(), requestDto.getConnectPort()));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("无法连接, 拒绝");
            }
            System.out.println(destChannel + " 准备连接 " + destChannel.isConnected());
            destChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT, new SocketWrapper(destChannel, this, true));

            System.out.println("register " + socketChannel);
            System.out.println(requestDto);
            socketWrapper.setReadHeader(true);
            socketBinds.put(socketChannel, destChannel, requestDto);
        } else {
            SocketChannel destSocket = socketBinds.findByEach(socketWrapper.getSocketChannel());
            if (destSocket.isConnected()) {
                boolean b = readToWrite(socketWrapper.getSocketChannel(), destSocket);
                if (!b) {
                    System.out.println("cancel");
                    cancel(selectionKey, socketChannel);
                    return;
                } else {
                    System.out.println("读到数据了 " + destSocket);
                }
            }
        }

    }

    public void reg(SelectionKey selectionKey, int ops) {
        selectionKey.interestOps(selectionKey.interestOps() | ops);
    }

    public void unreg(SelectionKey selectionKey, int ops) {
        selectionKey.interestOps(selectionKey.interestOps() ^ ops);
    }

    private static RequestDto getRequestDto(SocketChannel channel) throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate(2048);
        System.out.println("我准备读取requestDto " + channel);
        String content = "";
        while (content.indexOf("\r\n\r\n") < 0) {
            ByteBuffer eachByte = ByteBuffer.allocate(1);
            int read = channel.read(eachByte);
            allocate.put(eachByte.array(), eachByte.arrayOffset(), eachByte.position());
            content = new String(allocate.array(), allocate.arrayOffset(), allocate.position());
            if (content.contains("\r\n\r\n")) {
                RequestDto requestDto = new RequestDto(content);
                System.out.println(System.currentTimeMillis() + "=====   content  " + requestDto.getUrl());
                return requestDto;
            }
            if (read == -1) {
                return null;
            }
        }
        return null;
    }


    private void cancel(SelectionKey selectionKey, SocketChannel channel) throws IOException {
        if (selectionKey != null) {
            selectionKey.cancel();
        }
        channel.close();
        socketBinds.remove(channel);
        System.out.println("cancel!!!!!!!! " + channel);
    }


    private boolean readToWrite(SocketChannel channel, SocketChannel channel1) {
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
                    System.out.println(Thread.currentThread().getName() + "挂了准备!!!!  " + channel);
                    return !isFirst;
                }
                int total = 0;
                allocate.flip();
                System.out.println(Thread.currentThread().getName() + " 准备写入    " + channel1);
                while (allocate.hasRemaining()) {
                    channel1.write(allocate);
                    System.out.println(Thread.currentThread().getName() + " 轮训中.......");
                }
                System.out.println(read + "  " + total + "  " + allocate.limit());

                isFirst = false;
                System.out.println(Thread.currentThread().getName() + " =====  read  " + socketBinds.findRequestByEach(channel).getUrl());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
