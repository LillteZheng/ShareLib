package com.hht.sharelib.transtype.nio.core.impl;

import com.hht.sharelib.CloseUtils;
import com.hht.sharelib.transtype.nio.callback.IoProvider;
import com.hht.sharelib.transtype.nio.callback.Receiver;
import com.hht.sharelib.transtype.nio.callback.Sender;
import com.hht.sharelib.transtype.nio.core.IoArgs;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: sender 和 receiver 的接口实现类
 */
public class SocketChannelAdapter implements Sender,Receiver,Closeable{
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private IoProvider mIoProvider;
    private SocketChannel mChannel;
    private IoArgs mReceiveTempIoArgs;
    private IoArgs.IoArgsEventListener mReceiveIoArgsListener;
    private IoArgs.IoArgsEventListener mSendIoArgsListener;
    private ChannelCloseListener mListener;
    public SocketChannelAdapter(SocketChannel channel,IoProvider ioProvider,ChannelCloseListener listener){
        mChannel = channel;
        mIoProvider = ioProvider;
        mListener = listener;
    }


    @Override
    public void setReceiveListener(IoArgs.IoArgsEventListener listener) {
        mReceiveIoArgsListener = listener;
    }

    @Override
    public boolean receiveAsync(IoArgs ioArgs) throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed");
        }
        mReceiveTempIoArgs = ioArgs;
        return mIoProvider.registerInput(mChannel,inputCallback);
    }

    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed");
        }
        mSendIoArgsListener = listener;
        outputCallback.setAttach(args);
        return mIoProvider.registerOutput(mChannel,outputCallback);
    }



    private IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        public void canProviderOutput() {
            if (isClosed.get()){
                return;
            }

            IoArgs args = getAttach();

            IoArgs.IoArgsEventListener listener = mSendIoArgsListener;
            listener.onStart(args);
            try {
                if (args.writeTo(mChannel) > 0){
                    listener.onCompleted(args);
                }else{
                    throw new IOException("Cannot write any data");
                }
            } catch (IOException e) {
                //e.printStackTrace();
                CloseUtils.close(SocketChannelAdapter.this);
            }

        }
    };

    private IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {

        @Override
        public void canProviderInput() {
            if (isClosed.get()){
                return;
            }
            IoArgs args = mReceiveTempIoArgs;
            IoArgs.IoArgsEventListener listener = mReceiveIoArgsListener;

            listener.onStart(args);

            try {
                if (args.readFrom(mChannel) > 0){
                    listener.onCompleted(args);
                }else{
                    throw new IOException("Cannot read form any data");
                }
            } catch (IOException e) {
                //e.printStackTrace();
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false,true)){
            //解除注册
            mIoProvider.unRegisterInput(mChannel);
            mIoProvider.unRegisterOutput(mChannel);
            //关闭channel
            CloseUtils.close(mChannel);

            //通知已经关闭

            mListener.onChannelClosed(mChannel);

        }
    }

    public interface ChannelCloseListener {
        void onChannelClosed(SocketChannel channel);
    }
}
