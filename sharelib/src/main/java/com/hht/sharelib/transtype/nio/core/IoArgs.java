package com.hht.sharelib.transtype.nio.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 对bytebuffer 的封装，避免重复创建，增加内存消耗
 */
public class IoArgs {
    //byte限制
    private int mLimit = 512;
    private ByteBuffer mBuffer = ByteBuffer.allocate(mLimit);

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
     * 从 socketchannel 读数据到 bytebuffer
     * @param channel
     * @return
     * @throws IOException
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
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
     * 把bytebuffer的数据写到 socketchannel中
     * @param channel
     * @return
     */
    public int writeTo(WritableByteChannel channel) throws IOException {
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
        startWriting();
        mBuffer.putInt(length);
        finishWriting();
    }

    public int readLength(){
        return mBuffer.getInt();
    }

    public int capacity(){
        return mBuffer.capacity();
    }


    /**
     * IoArgs 提供者、处理者；数据的生产或消费者
     */
    public interface IoArgsEventProcessor {
        /**
         * 提供一份可消费的IoArgs
         *
         * @return IoArgs
         */
        IoArgs provideIoArgs();

        /**
         * 消费失败时回调
         *
         * @param args IoArgs
         * @param e    异常信息
         */
        void onConsumeFailed(IoArgs args, Exception e);

        /**
         * 消费成功
         *
         * @param args IoArgs
         */
        void onConsumeCompleted(IoArgs args);
    }
}
