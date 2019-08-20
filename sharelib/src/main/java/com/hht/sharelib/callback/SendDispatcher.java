package com.hht.sharelib.callback;

import com.hht.sharelib.transtype.nio.packet.SendPacket;

import java.io.Closeable;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe:
 */
public interface SendDispatcher extends Closeable{
    void send(SendPacket packet);
    void cancel(SendPacket packet);
}
