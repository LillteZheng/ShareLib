package com.hht.sharelib.callback;

import com.hht.sharelib.transtype.nio.packet.ReceivePacket;

import java.io.Closeable;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe:
 */
public interface ReceiveDispatcher extends Closeable {
    void start();
    void stop();
    interface ReceivePacketCallback {
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
