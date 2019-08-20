package com.hht.sharelib.transtype.nio.core.impl.async;

import com.hht.sharelib.CloseUtils;
import com.hht.sharelib.callback.ReceiveDispatcher;
import com.hht.sharelib.transtype.nio.callback.Receiver;
import com.hht.sharelib.transtype.nio.core.IoArgs;
import com.hht.sharelib.transtype.nio.packet.ReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.StringReceivePacket;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 异步接收实现类，处理黏包和分包问题
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher {
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private Receiver mReceiver;
    private IoArgs mIoArgs = new IoArgs();
    private ReceivePacket mTempPacket;
    private int mPosition;
    private int mTotal;
    private byte[] mBuffer ;
    private ReceivePacketCallback mCallback;
    public AsyncReceiveDispatcher(Receiver receiver,ReceivePacketCallback callback) {
        this.mReceiver = receiver;
        mCallback = callback;
        mReceiver.setReceiveListener(receiveEventListener);
    }

    @Override
    public void start() {
        registerReceive();
    }

    private void registerReceive(){
        try {
            mReceiver.receiveAsync(mIoArgs);
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
            mTempPacket = new StringReceivePacket(length);
            mBuffer = new byte[length];
            mTotal = length;
            mPosition = 0;
        }
        //把数据从 bytebuffer 读到 byte 中
        int count = args.writeTo(mBuffer, 0);
        //有数据
        if (count > 0){
            mTempPacket.save(mBuffer,count);
            mPosition += mTotal;
            //读取完毕，通知出去
            if (mPosition == mTotal){
                completePacket();
            }
        }
    }

    private void completePacket() {
        ReceivePacket packet = mTempPacket;
        CloseUtils.close(packet);
        mCallback.onReceivePacketCompleted(packet);
        mTempPacket = null;
    }

    IoArgs.IoArgsEventListener receiveEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStart(IoArgs args) {
            int receiveSize;
            if (mTempPacket == null){
                //头部长度，4个字节
                receiveSize = 4;
            }else{
                receiveSize = Math.min(mTotal - mPosition,args.capacity());
            }
            args.limit(receiveSize);
        }

        @Override
        public void onCompleted(IoArgs args) {
            //解析数据
            assemblePacket(args);
            //读下一条数据
            registerReceive();
        }


    };
}
