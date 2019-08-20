package com.hht.sharelib.transtype.nio.packet;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 发送类型数据定义
 */
public abstract class SendPacket extends Packet {
    private boolean isCancled;
    public abstract byte[] bytes();

    public boolean isCancled() {
        return isCancled;
    }
}
