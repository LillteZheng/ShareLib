package com.hht.sharelib.transtype.nio.packet;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe:接收数据定义
 */
public abstract class ReceivePacket<T extends OutputStream,Entity> extends Packet<T> {
    private Entity entity;
    public ReceivePacket(long len){
        length = len;
    }

    public Entity entity() {
        return entity;
    }

    /**
     * 让子类去决定返回什么类型
     * @param stream
     * @return
     */
    protected abstract Entity buildEntity(T stream);

    @Override
    protected void closeStream(T stream) {
        super.closeStream(stream);
        entity = buildEntity(stream);
    }

}
