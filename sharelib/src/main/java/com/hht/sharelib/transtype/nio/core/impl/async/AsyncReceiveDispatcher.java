package com.hht.sharelib.transtype.nio.core.impl.async;

import com.hht.sharelib.transtype.nio.packet.Packet;
import com.hht.sharelib.utils.CloseUtils;
import com.hht.sharelib.callback.ReceiveDispatcher;
import com.hht.sharelib.transtype.nio.callback.Receiver;
import com.hht.sharelib.transtype.nio.core.IoArgs;
import com.hht.sharelib.transtype.nio.packet.ReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.StringReceivePacket;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 异步接收实现类，处理黏包和分包问题
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher, IoArgs.IoArgsEventProcessor {
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private Receiver mReceiver;
    private IoArgs mIoArgs = new IoArgs();
    private ReceivePacket<?,?> mTempPacket;
    private int mPosition;
    private int mTotal;
    private ReceivePacketCallback mCallback;
    private WritableByteChannel mByteChannel;
    public AsyncReceiveDispatcher(Receiver receiver,ReceivePacketCallback callback) {
        this.mReceiver = receiver;
        mCallback = callback;
        mReceiver.setReceiveListener(this);
    }

    @Override
    public void start() {
        registerReceive();
    }



    private void registerReceive(){
        try {
            mReceiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }



    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            ReceivePacket packet = mTempPacket;
            CloseUtils.close(packet);
            mTempPacket = null;
        }
    }

    /**
     * 数据解析 header+data
     * @param args
     */
    private void assemblePacket(IoArgs args) {
        //首包
        if (mTempPacket == null){
            int length = args.readLength();
            //判断是文件还是字符串
            byte type = length > 256 ? Packet.TYPE_STREAM_FILE : Packet.TYPE_MEMORY_STRING;
            //mTempPacket = new StringReceivePacket(length);
            mTempPacket = mCallback.onArrivedNewPacket(type,length);
            mByteChannel = Channels.newChannel(mTempPacket.open());
            mTotal = length;
            mPosition = 0;
        }
        try {
            //把数据从 bytebuffer 读到 byte 中
            int count = args.writeTo(mByteChannel);

            mPosition += count;
            //读取完毕，通知出去
            if (mPosition == mTotal){
                completePacket(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            completePacket(false);
        }
    }

    private void completePacket(boolean isSuccess) {
        ReceivePacket packet = mTempPacket;
        CloseUtils.close(packet);
        mTempPacket = null;
        WritableByteChannel channel = this.mByteChannel;
        CloseUtils.close(channel);
        mByteChannel = null;
        if (packet != null) {
            mCallback.onReceivePacketCompleted(packet);
        }
    }



    @Override
    public IoArgs provideIoArgs() {
        int receiveSize;
        IoArgs args = this.mIoArgs;
        if (mTempPacket == null){
            //头部长度，4个字节
            receiveSize = 4;
        }else{
            receiveSize = Math.min(mTotal - mPosition, args.capacity());
        }
        args.limit(receiveSize);
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //解析数据
        assemblePacket(args);
        //读下一条数据
        registerReceive();
    }
}
