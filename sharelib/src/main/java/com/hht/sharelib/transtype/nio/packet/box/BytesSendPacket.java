package com.hht.sharelib.transtype.nio.packet.box;

import com.hht.sharelib.transtype.nio.packet.SendPacket;

import java.io.ByteArrayInputStream;

/**
 * @author by  zhengshaorui on 2019/9/10
 * Describe: 基类，以byte数组来实现不同stream的类型
 */
public class BytesSendPacket extends SendPacket<ByteArrayInputStream> {

    private final byte[] bytes;

    public BytesSendPacket(byte[] bytes) {
        this.bytes = bytes;
        length = bytes.length;
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_BYTES;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}
