package com.hht.sharelib.transtype.nio.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 对bytebuffer 的封装，避免重复创建，增加内存消耗
 */
public class IoArgs {
    //byte限制
    private int mLimit = 256;
    private final byte[] BYTES = new byte[mLimit];
    private ByteBuffer mBuffer = ByteBuffer.wrap(BYTES);

    /**
     * 从 socketchannel 读数据到 bytebuffer
     * @param channel
     * @return
     * @throws IOException
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int length = 0;
        while (mBuffer.hasRemaining()) {
            int read = channel.read(mBuffer);
            if (read < 0) {
                throw new EOFException();
            }
            length += read;
        }
        finishWriting();
        return length;
    }

    /**
     * 从byte数组读数据到bytebuffer
     * @param bytes
     * @param offset
     * @return
     */
    public int readFrom(byte[] bytes,int offset){
        //拿到当前bytebuffer可填充的数量
        int size = Math.min(bytes.length - offset,mBuffer.remaining());
        mBuffer.put(bytes,offset,size);
        return size;
    }

    /**
     * 把bytebuffer的数据写到 socketchannel中
     * @param channel
     * @return
     */
    public int writeTo(SocketChannel channel) throws IOException {
        int length = 0;
        while (mBuffer.hasRemaining()){
            int write = channel.write(mBuffer);
            if (write < 0){
                throw new  EOFException();
            }
            length += write;
        }
        return length;
    }

    /**
     * 把bytebuffer的数据写到byte中
     * @param bytes
     * @param offset
     * @return
     */
    public int writeTo(byte[] bytes,int offset){
        //拿到当前bytebuffer可填充的数量
        int size = Math.min(bytes.length - offset,mBuffer.remaining());
        mBuffer.get(bytes,offset,size);
        return size;
    }


    /**
     * 设置bytebuffer 的limit
     * @param limit
     */
    public void limit(int limit){
        mLimit = limit;
    }

    /**
     * 开始bytebuffer的编写
     */
    public void startWriting(){
        mBuffer.clear();
        mBuffer.limit(mLimit);
    }

    /**
     * 切换成读模式
     */
    public void finishWriting(){
        mBuffer.flip();
    }

    public void writeLength(int length) {
        mBuffer.putInt(length);
    }

    public int readLength(){
        return mBuffer.getInt();
    }

    public int capacity(){
        return mBuffer.capacity();
    }

    public interface IoArgsEventListener{
        void onStart(IoArgs args);
        void onCompleted(IoArgs args);
    }
}
