package com.hht.sharelib.transtype.nio.packet;

import java.io.InputStream;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 发送类型数据定义
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T> {
    private boolean isCancled;

    public boolean isCancled() {
        return isCancled;
    }

}
