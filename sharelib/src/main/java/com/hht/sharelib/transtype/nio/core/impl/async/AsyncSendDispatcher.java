package com.hht.sharelib.transtype.nio.core.impl.async;

import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.hht.sharelib.CloseUtils;
import com.hht.sharelib.callback.SendDispatcher;
import com.hht.sharelib.transtype.nio.callback.Sender;
import com.hht.sharelib.transtype.nio.core.IoArgs;
import com.hht.sharelib.transtype.nio.packet.SendPacket;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 异步发送类具体实现，处理黏包和分包问题
 */
public class AsyncSendDispatcher implements SendDispatcher{
    private AtomicBoolean isSending = new AtomicBoolean();
    private AtomicBoolean isClosed = new AtomicBoolean();
    private Queue<SendPacket> mQueue;
    private SendPacket mTempPacket;

    private int mPosition;
    private int mTotal;
    private IoArgs mIoArgs = new IoArgs();

    private Sender mSender;

    public AsyncSendDispatcher(Sender sender) {
        this.mSender = sender;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mQueue = new ConcurrentLinkedDeque<>();
        }else{
            mQueue = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public void send(SendPacket packet) {
        if (isClosed.get()){
            return;
        }
        mQueue.offer(packet);
        if (isSending.compareAndSet(false,true)){
            sendNextMsg();
        }
    }

    private SendPacket takePacket(){
        SendPacket temp = mQueue.poll();
        if (temp != null && temp.isCancled()){
            //已取消，取下一条
            return takePacket();
        }
        return temp;
    }

    private void sendNextMsg(){
        SendPacket temp = mTempPacket;
        if (temp != null){
            CloseUtils.close(temp);
        }

        SendPacket packet = mTempPacket = takePacket();
        if (packet == null){
            //可以继续发送了，重置状态
            isSending.set(false);
            return;
        }
        mPosition = 0;
        mTotal = packet.length();

        //开始发送
        sendCurrentPacket();
    }

    /**
     * 发送真正的数据
     */
    private void sendCurrentPacket() {
        //先拿到 ioArgs
        IoArgs args = mIoArgs;

        args.startWriting();

        //判断是否发送完
        if (mPosition >= mTotal){
            sendNextMsg();
            return;
        }
        //这个是首包，需要把长度信息写上，即占四个字节
        if (mPosition == 0){
            args.writeLength(mTotal);
        }

        //拿真正的数据
        byte[] bytes = mTempPacket.bytes();
        int count = args.readFrom(bytes, mPosition);

        //记录标志，如果buffer不够，则继续填充数据
        mPosition += count;

        args.finishWriting();

        try {
            mSender.sendAsync(args,sendEventListener);
        } catch (IOException e) {
           closeAndNotify();
        }

    }

    IoArgs.IoArgsEventListener sendEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStart(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            //通过这种循环，可以让一个大数据，根据 buffer 大小去发送
            sendCurrentPacket();
        }
    };

    @Override
    public void cancel(SendPacket packet) {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            isSending.set(false);
            SendPacket packet = mTempPacket;
            CloseUtils.close(packet);
            mTempPacket = null;
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }
}
