package com.hht.sharelib.transtype.nio.packet.box;

import com.hht.sharelib.transtype.nio.packet.ReceivePacket;

import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 字符接收数据类
 */
public class StringReceivePacket extends ReceivePacket {
    private final byte[] buffer;
    private int position;
    public StringReceivePacket(int len){
        buffer = new byte[len];
        length = len;
        position = 0;
    }
    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes,0,buffer,position,count);
        position += count;
    }

    public String string(){
        return new String(buffer);
    }

    @Override
    public void close() throws IOException {

    }
}
