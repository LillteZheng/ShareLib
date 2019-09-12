package com.hht.sharelib.transtype.nio.packet.box;

import com.hht.sharelib.transtype.nio.packet.ReceivePacket;

import java.io.ByteArrayOutputStream;

/**
 * @author by  zhengshaorui on 2019/9/10
 * Describe:
 */
public abstract class BytesReceivePacket<T> extends ReceivePacket<ByteArrayOutputStream,T> {
    public BytesReceivePacket(long len) {
        super(len);
    }


    @Override
    public byte type() {
        return TYPE_MEMORY_BYTES;
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
