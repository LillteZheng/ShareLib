package com.hht.sharelib.transtype.nio.packet;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe:接收数据定义
 */
public abstract class ReceivePacket extends Packet {
    public abstract void save(byte[] bytes,int count);
}
