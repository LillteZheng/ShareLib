package com.hht.sharelib.transtype.nio.core.impl;

import com.hht.sharelib.utils.CloseUtils;
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
    private IoArgs.IoArgsEventProcessor mReceiveIoArgsProcessor;
    private IoArgs.IoArgsEventProcessor mSendIoArgsProcessor;
    private ChannelCloseListener mListener;
    public SocketChannelAdapter(SocketChannel channel,IoProvider ioProvider,ChannelCloseListener listener){
        mChannel = channel;
        mIoProvider = ioProvider;
        mListener = listener;
    }

    @Override
    public void setReceiveListener(IoArgs.IoArgsEventProcessor processor) {
        mReceiveIoArgsProcessor = processor;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed");
        }
        return mIoProvider.registerInput(mChannel,inputCallback);
    }

    @Override
    public void setSenderListener(IoArgs.IoArgsEventProcessor processor) {
        mSendIoArgsProcessor = processor;
    }


    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()){
            throw new IOException("Current channel is closed");
        }
        return mIoProvider.registerOutput(mChannel,outputCallback);
    }

    private IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        public void canProviderOutput() {
            if (isClosed.get()){
                return;
            }

            IoArgs.IoArgsEventProcessor processor = mSendIoArgsProcessor;
            IoArgs args = processor.provideIoArgs();
            try {
                if (args.writeTo(mChannel) > 0){
                    processor.onConsumeCompleted(args);
                }else{
                    processor.onConsumeFailed(args,new IOException("Cannot write any data"));
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
            IoArgs.IoArgsEventProcessor processor = mReceiveIoArgsProcessor;
            IoArgs args = processor.provideIoArgs();


            try {
                if (args.readFrom(mChannel) > 0){
                    processor.onConsumeCompleted(args);
                }else{
                    processor.onConsumeFailed(args,new IOException("Cannot read form any data"));
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
