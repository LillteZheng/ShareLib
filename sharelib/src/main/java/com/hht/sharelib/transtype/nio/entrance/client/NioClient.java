package com.hht.sharelib.transtype.nio.entrance.client;

import android.util.Log;

import com.hht.sharelib.CloseUtils;
import com.hht.sharelib.ShareManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.ClientListener;
import com.hht.sharelib.transtype.Client;
import com.hht.sharelib.transtype.nio.IoContext;
import com.hht.sharelib.transtype.nio.core.Connector;
import com.hht.sharelib.transtype.nio.core.impl.IoSelectortProvider;
import com.hht.sharelib.transtype.socket.TCPConstants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: NIO客户端
 */
public class NioClient extends Connector implements Client {
    private static final String TAG = "NioClient";
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private ClientListener mResponseListener;
    private DeviceInfo mInfo;

    @Override
    public void bindWidth(final String ip, final ClientListener listener){
        mResponseListener = listener;
        try {
            IoContext.setUp()
                    .ioProvider(new IoSelectortProvider())
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "zsr bindWidth error  "+e.getMessage());
        }

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketChannel socket = SocketChannel.open();
                    socket.connect(new InetSocketAddress(InetAddress.getByName(ip),TCPConstants.PORT_SERVER));
                    setUp(socket);
                    //表示连接成功
                    connectSuccess(socket);
                } catch (final IOException e) {
                    //e.printStackTrace();
                    ShareManager.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.serverConnectFail(e.toString());
                        }
                    });
                }
            }
        });
    }

    private void connectSuccess(final SocketChannel socket) {
        if (mResponseListener != null){
            ShareManager.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String ip = socket.socket().getInetAddress().getHostAddress();
                    int port = socket.socket().getPort();
                    mInfo = new DeviceInfo();
                    mInfo.ip = ip;
                    mInfo.port = port;
                    mInfo.info = "server connect success";
                    mResponseListener.serverConnected(mInfo);
                }
            });
        }
    }


    @Override
    public void stop(){
        IoContext.close();
        CloseUtils.close(this);
    }

    @Override
    public void sendMsg(String msg) {
        send(msg);
    }

    @Override
    public void onChannelClosed(final SocketChannel channel) {
        super.onChannelClosed(channel);
        if (mResponseListener != null){
            ShareManager.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    mInfo.info = "server disconnect";
                    mResponseListener.serverDisconnect(mInfo);
                }
            });
        }
    }

    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
        if (mResponseListener != null){
            mResponseListener.onResponse(str);
        }
    }
}
