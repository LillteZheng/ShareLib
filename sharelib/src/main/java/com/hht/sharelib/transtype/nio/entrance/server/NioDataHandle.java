package com.hht.sharelib.transtype.nio.entrance.server;

import android.text.TextUtils;
import android.util.Log;

import com.hht.sharelib.transtype.nio.packet.ReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.StringReceivePacket;
import com.hht.sharelib.utils.CloseUtils;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.transtype.nio.core.Connector;
import com.hht.sharelib.utils.Foo;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioDataHandle extends Connector{
    private static final String TAG = "NioDataHandle";
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private static final String SERVER = "server";
    private DataListener mListener;
    private  DeviceInfo mInfo;
    private SocketChannel mSocketChannel;
    private NioServer mNioServer;
    public NioDataHandle(SocketChannel socket, DataListener listener,NioServer nioServer) {
        mSocketChannel = socket;
        mNioServer = nioServer;
        mListener = listener;
        try {
            setUp(socket);
            String ip = socket.socket().getInetAddress().getHostAddress();
            int port = socket.socket().getPort();
            mInfo = new DeviceInfo(ip, port, "client connect");
            mListener.onConnect(mInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void exitBySelt(){
        exit();
        if (mListener != null){
            mListener.onSelfClosed(this);
        }
    }

    public void exit() {
        CloseUtils.close(this);
        CloseUtils.close(mSocketChannel);
        //同时也要清掉保存的缓存文件
        Foo.deleteFolder(SERVER);
    }



    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelt();

    }



    @Override
    protected void onReceivePacket(ReceivePacket packet) {
        super.onReceivePacket(packet);
        mListener.onResponse(this,packet);
    }



    @Override
    protected File createNewReceiveFile() {
        String name = mNioServer.mFileName;
        //用temp表示临时标量
        if (!TextUtils.isEmpty(name)){
            name = ".tmp."+name.substring(name.lastIndexOf(".")+1);
            File file = Foo.createNewFile(SERVER, name);
            mNioServer.mFileName = null;
            return file;
        }
        return Foo.createNewFile(SERVER,".test.tmp");
    }

    public DeviceInfo getInfo(){
        return mInfo;
    }

    public interface DataListener {
        void onResponse(NioDataHandle handle, ReceivePacket packet);
        void onSelfClosed(NioDataHandle handle);
        void onConnect(DeviceInfo info);
    }


}
