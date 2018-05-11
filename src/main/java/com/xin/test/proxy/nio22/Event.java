package com.xin.test.proxy.nio22;

import lombok.Data;

import java.nio.channels.SocketChannel;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Data
public class Event {
    private EventTypeEnum eventType;
    private SocketChannel socketChannel;

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public enum EventTypeEnum {
        REGISTER, READ, WRITE
    }
}
