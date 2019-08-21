package com.hht.sharelib.transtype.socket.tcp.client;

import com.hht.sharelib.ShareManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.ClientListener;
import com.hht.sharelib.transtype.Client;
import com.hht.sharelib.transtype.socket.TCPConstants;
import com.hht.sharelib.transtype.socket.tcp.DataHandle;
import com.hht.sharelib.CloseUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: tcp 客户端，用来接收和发送,接收、读、写，需要分离，避免干扰
 */
public class TcpClient implements DataHandle.DataListener,Client {
    private static final String TAG = "TcpClient";
    private ExecutorService mExecutorService;
    private Socket mSocket;
    private DataHandle mDataHandle;

    public static TcpClient create(){
        return new TcpClient();
    }

    private TcpClient(){
        mExecutorService = Executors.newSingleThreadExecutor();
    }


    @Override
    public void bindWidth(final String ip, final ClientListener listener) {
        mResponseListener = listener;
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                //由系统指定端口
                try {
                    mSocket = new Socket();

                    mSocket.connect(new InetSocketAddress(InetAddress.getByName(ip), TCPConstants.PORT_SERVER));
                    InetAddress ip = mSocket.getLocalAddress();
                    int port = mSocket.getPort();
                    mDataHandle = new DataHandle(mSocket,TcpClient.this);

                } catch (final IOException e) {
                    e.printStackTrace();
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

    @Override
    public void stop(){
        if (mSocket != null){
            CloseUtils.close(mSocket);
        }
        mDataHandle.exit();
    }

    @Override
    public void sendMsg(String msg) {
        if (mDataHandle != null){
            mDataHandle.sendMsg(msg);
        }
    }


    @Override
    public void onResponse(DataHandle handle, String msg) {
        if (mResponseListener != null){
            mResponseListener.onResponse(msg);
        }
    }

    @Override
    public void disConnect(final DataHandle handle) {
        if (mResponseListener != null){
            ShareManager.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    DeviceInfo info = handle.getInfo();
                    info.info = "server disconnect";
                    mResponseListener.serverDisconnect(info);
                }
            });

        }
    }

    @Override
    public void onSelfClosed(DataHandle handle) {
        handle.exit();
    }

    @Override
    public void onConnect(final DeviceInfo info) {
        ShareManager.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (mResponseListener != null) {
                    info.info = "server connected";
                    mResponseListener.serverConnected(info);
                }
            }
        });


    }

    private ClientListener mResponseListener;
    public void addResponseListener(ClientListener listener){
        mResponseListener = listener;
    }
}
