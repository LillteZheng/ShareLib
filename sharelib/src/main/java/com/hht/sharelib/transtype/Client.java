package com.hht.sharelib.transtype;

import com.hht.sharelib.callback.TcpClientListener;
import com.hht.sharelib.transtype.nio.packet.SendPacket;

import java.io.File;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 客户端统一接口
 */
public  interface Client {
    /**
     * 绑定服务端
     * @param ip
     * @param listener
     */
    void bindWidth(final String ip, final TcpClientListener listener);

    /**
     * 释放一些资源
     */
    void stop();

    /**
     * 发送数据
     * @param msg
     */
    void sendMsg(String msg);

    /**
     * 发送数据包
     */
    void sendFile(File file);
}
