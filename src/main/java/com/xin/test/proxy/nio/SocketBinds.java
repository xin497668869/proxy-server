package com.xin.test.proxy.nio;

import com.xin.test.proxy.RequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class SocketBinds {

    private List<SocketBind> socketBinds = new ArrayList<>();

    public synchronized void put(SocketChannel leftContent, SocketChannel proxySocket, RequestDto requestDto) {
        socketBinds.add(new SocketBind(proxySocket, leftContent, requestDto));
    }

    public synchronized void remove(SocketChannel socketChannel) {
        Iterator<SocketBind> iterator = socketBinds.iterator();
        while (iterator.hasNext()) {
            SocketBind socketBind = iterator.next();
            if (socketChannel.equals(socketBind.getProxySocket()) || socketChannel.equals(socketBind.getDestSocket())) {
                iterator.remove();
                return;
            }

        }
    }

    public RequestDto findRequestByEach(SocketChannel socketChannel) {
        for (SocketBind socketBind : socketBinds) {
            if (socketChannel.equals(socketBind.getProxySocket())) {
                return socketBind.getRequestDto();
            }
            if (socketChannel.equals(socketBind.getDestSocket())) {
                return socketBind.getRequestDto();
            }
        }
        return null;
    }

    public SocketChannel findByEach(SocketChannel socketChannel) {
        for (SocketBind socketBind : socketBinds) {
            if (socketChannel.equals(socketBind.getProxySocket())) {
                return socketBind.getDestSocket();
            }
            if (socketChannel.equals(socketBind.getDestSocket())) {
                return socketBind.getProxySocket();
            }
        }
        return null;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocketBind {
        private SocketChannel proxySocket;
        private SocketChannel destSocket;
        private RequestDto    requestDto;
    }

}
