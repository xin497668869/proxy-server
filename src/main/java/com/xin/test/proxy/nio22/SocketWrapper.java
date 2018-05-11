package com.xin.test.proxy.nio22;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.channels.SocketChannel;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketWrapper {
    private SocketChannel socketChannel;
    private Poll          poll;
    private boolean       readHeader = false;
}
