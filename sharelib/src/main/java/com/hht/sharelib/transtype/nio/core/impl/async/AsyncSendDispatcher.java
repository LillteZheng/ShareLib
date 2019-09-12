package com.hht.sharelib.transtype.nio.core.impl.async;

import android.os.Build;

import com.hht.sharelib.utils.CloseUtils;
import com.hht.sharelib.callback.SendDispatcher;
import com.hht.sharelib.transtype.nio.callback.Sender;
import com.hht.sharelib.transtype.nio.core.IoArgs;
import com.hht.sharelib.transtype.nio.packet.SendPacket;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 异步发送类具体实现，处理黏包和分包问题
 */
public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcessor {
    private AtomicBoolean isSending = new AtomicBoolean();
    private AtomicBoolean isClosed = new AtomicBoolean();
    private Queue<SendPacket> mQueue;
    private SendPacket<?> mTempPacket;

    private long mPosition;
    private long mTotal;
    private IoArgs mIoArgs = new IoArgs();
    private Sender mSender;
    private ReadableByteChannel mByteChannel;

    public AsyncSendDispatcher(Sender sender) {
        this.mSender = sender;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mQueue = new ConcurrentLinkedDeque<>();
        }else{
            mQueue = new ConcurrentLinkedQueue<>();
        }
        mSender.setSenderListener(this);
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
     * 发送真正的数据,在发送之前，先检测是否发送完成
     * 当未发送完成，重新注册即可
     */
    private void sendCurrentPacket() {

        //判断是否发送完
        if (mPosition >= mTotal){
            completePacket(mPosition == mTotal);
            sendNextMsg();
            return;
        }
        try {
            mSender.postSendAsync();
        } catch (IOException e) {
           closeAndNotify();
        }

    }

    private void completePacket(boolean isSuccess) {
        SendPacket<?> packet = this.mTempPacket;
        CloseUtils.close(packet);
        mTempPacket = null;

        ReadableByteChannel channel = this.mByteChannel;
        CloseUtils.close(channel);
        mByteChannel = null;
        mTotal = 0;
        mPosition = 0;
    }



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

    @Override
    public IoArgs provideIoArgs() {

        //先拿到 ioArgs
        IoArgs args = mIoArgs;

        if (mByteChannel == null){
            mByteChannel = Channels.newChannel(mTempPacket.open());
            args.limit(4);
            //todo 字符串先用这个测试
            args.writeLength((int) mTempPacket.length());
        }else{
            try {
                args.limit((int) Math.min(args.capacity(),mTotal - mPosition));
                int count = args.readFrom(mByteChannel);
                //记录标志，如果buffer不够，则继续填充数据
                mPosition += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //通过这种循环，可以让一个大数据，根据 buffer 大小去发送
        sendCurrentPacket();
    }
}
