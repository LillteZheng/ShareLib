package com.hht.sharelib.transtype.nio.packet.box;

import com.hht.sharelib.transtype.nio.packet.SendPacket;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 字符串类型数据
 */
public class StringSendPacket extends BytesSendPacket{

    public StringSendPacket(String msg) {
        super(msg.getBytes());
    }


    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }


}
