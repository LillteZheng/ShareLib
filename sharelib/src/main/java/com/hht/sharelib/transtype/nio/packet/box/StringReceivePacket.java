package com.hht.sharelib.transtype.nio.packet.box;

import com.hht.sharelib.transtype.nio.packet.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 字符接收数据类
 */
public class StringReceivePacket extends BytesReceivePacket<String>{

    public StringReceivePacket(long len) {
        super(len);
    }

    @Override
    protected String buildEntity(ByteArrayOutputStream stream) {
        return new String(stream.toByteArray());
    }


}
