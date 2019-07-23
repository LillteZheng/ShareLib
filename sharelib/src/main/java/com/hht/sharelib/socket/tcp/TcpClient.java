package com.hht.sharelib.socket.tcp;

import com.hht.sharelib.ShareConstants;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.TcpClientListener;
import com.hht.sharelib.socket.DataHandle;
import com.hht.sharelib.NetConfig;
import com.hht.sharelib.tools.CloseUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpClient implements DataHandle.DataReadListener {


    private Socket mSocket;

    private DataHandle mDataHandle;
    private ExecutorService mExecutorService ;

    private TcpClient(String ip) {
        mExecutorService = Executors.newFixedThreadPool(3);
        startConnectServer(ip);

    }
    public static TcpClient create(String ip){
        return new TcpClient(ip);
    }




    public void exit(){
        CloseUtils.close(mSocket);
        mExecutorService.shutdownNow();
    }


    private void startConnectServer(final String ip){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket == null){
                        mSocket = new Socket();
                    }
                    mSocket.setKeepAlive(true);
                    mSocket.connect(new InetSocketAddress(InetAddress.getByName(ip), NetConfig.getNetBean().tcpPort));

                    mDataHandle = new DataHandle(mSocket,TcpClient.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }


    /**
     * 发送数据
     * @param data
     */
    public void sendData(final String data) {
        if (mDataHandle != null){
            mDataHandle.sendMsg(data);
        }
    }
    private TcpClientListener mResponseListener;
    public void addResponseListener(BaseListener listener){
        mResponseListener = (TcpClientListener) listener;
    }




    @Override
    public void onResponse(DataHandle handle, String msg) {
        if (mResponseListener != null){
            if (msg.contains(ShareConstants.CONNECTED) &&
                    msg.contains(mSocket.getLocalAddress().getHostAddress())){
                ShareConstants.HANDLE.post(new Runnable() {
                    @Override
                    public void run() {
                        mResponseListener.connectServer(mSocket);
                    }
                });
            }else {
                mResponseListener.onResponse(msg);
            }
        }
    }

    @Override
    public void disConnect(DataHandle handle) {
        if (mResponseListener != null){
            ShareConstants.HANDLE.post(new Runnable() {
                @Override
                public void run() {
                    mResponseListener.serverDisconnect(mSocket);
                }
            });

        }
    }




}
