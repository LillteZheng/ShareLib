package com.hht.sharelib.transtype.nio.core;

import android.util.Log;

import com.hht.sharelib.callback.ReceiveDispatcher;
import com.hht.sharelib.callback.SendDispatcher;
import com.hht.sharelib.transtype.nio.IoContext;
import com.hht.sharelib.transtype.nio.callback.Receiver;
import com.hht.sharelib.transtype.nio.callback.Sender;
import com.hht.sharelib.transtype.nio.core.impl.SocketChannelAdapter;
import com.hht.sharelib.transtype.nio.core.impl.async.AsyncReceiveDispatcher;
import com.hht.sharelib.transtype.nio.core.impl.async.AsyncSendDispatcher;
import com.hht.sharelib.transtype.nio.packet.Packet;
import com.hht.sharelib.transtype.nio.packet.ReceivePacket;
import com.hht.sharelib.transtype.nio.packet.SendPacket;
import com.hht.sharelib.transtype.nio.packet.box.FileReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.StringReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.StringSendPacket;
import com.hht.sharelib.utils.CloseUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 发送接收接口
 */
public abstract class Connector implements SocketChannelAdapter.ChannelCloseListener,Closeable {
    private static final String TAG = "Connector";
    private Sender mSender;
    private Receiver mReceiver;
    private SendDispatcher mSendDispatcher;
    private ReceiveDispatcher mReceiveDispatcher;
    public void setUp(SocketChannel channel) throws IOException {
        //配置成非阻塞模式
        channel.configureBlocking(false);
        IoContext ioContext = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel,ioContext.getIoProvider(),this);
        mSender = adapter;
        mReceiver = adapter;

        mSendDispatcher = new AsyncSendDispatcher(mSender);
        mReceiveDispatcher = new AsyncReceiveDispatcher(mReceiver,receivePacketCallback);
        mReceiveDispatcher.start();
    }

    public void send(String msg){
        SendPacket packet = new StringSendPacket(msg);
        mSendDispatcher.send(packet);
    }

    public void sendPacket(SendPacket packet){
        mSendDispatcher.send(packet);
    }



    @Override
    public void onChannelClosed(SocketChannel channel) {
        //供外部调用
    }



    protected void onReceivePacket(ReceivePacket packet) {
        //供外部调用
    }

    protected abstract File createNewReceiveFile();

    ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback() {
        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {
            onReceivePacket(packet);
        }

        @Override
        public ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length) {
            switch (type){
                case Packet.TYPE_MEMORY_STRING:
                    return new StringReceivePacket(length);
                case Packet.TYPE_STREAM_FILE:
                    //发送过来的文件临时存储地点，由外部实现
                    return new FileReceivePacket(length,createNewReceiveFile());
                    default:
                        throw new RuntimeException("cannot found type: "+type);
            }
        }
    };

    @Override
    public void close() throws IOException {
       if (mReceiveDispatcher != null) {
           mReceiveDispatcher.close();
       }
       if (mSendDispatcher != null) {
           mSendDispatcher.close();
       }
       if (mSender != null) {
           mSender.close();
       }
       if (mReceiver != null) {
           mReceiver.close();
       }
    }
}
