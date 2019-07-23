package com.hht.sharelib.socket.tcp;

import android.util.Log;

import com.hht.sharelib.ShareConstants;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.TcpServerListener;
import com.hht.sharelib.socket.DataHandle;
import com.hht.sharelib.NetConfig;
import com.hht.sharelib.tools.CloseUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui
 * time on 2019/7/4
 * TCP服务器端
 */
public class TcpServer implements DataHandle.DataReadListener {
    private static final String TAG = "TcpServer";

    private ServerSocket mServerSocket;
    private Socket mClientSocket;
    private boolean isFinish = false;
    private List<DataHandle> mClientHandles = new ArrayList<>();
    private DataHandle mDataHandle;
    private TcpServerListener mResponseListener;
    private ExecutorService mServerExecutor;

    private TcpServer() {
        mServerExecutor = Executors.newFixedThreadPool(3);
        mServerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                startServer();
            }
        });

    }


    /**
     * 退出服务器
     */
    public synchronized void exit(){
        isFinish = true;
        CloseUtils.close(mServerSocket);
        CloseUtils.close(mClientSocket);
        for (DataHandle handle : mClientHandles) {
            handle.exit();
        }
        mClientHandles.clear();
        if (mServerExecutor != null){
            mServerExecutor.shutdownNow();
        }

    }
    public static TcpServer create(){
        return new TcpServer();
    }


    /**
     * 启动服务器
     */
    private void startServer(){
        try {
            mServerSocket = new ServerSocket(NetConfig.getNetBean().tcpPort);
            mServerSocket.setReuseAddress(true);
            do {
                mClientSocket = mServerSocket.accept();
                mDataHandle = new DataHandle(mClientSocket, TcpServer.this);
                synchronized (TcpServer.this){
                   mClientHandles.add(mDataHandle);
                    if (mResponseListener != null){
                        mResponseListener.onClientConnected(mClientSocket);
                        mResponseListener.clientCount(mClientHandles.size());
                    }
                }
                //此时已经连接到服务器了，发送已连接信息过去
                String msg = mClientSocket.getInetAddress().getHostAddress()+"/"+ mClientSocket.getPort()+"/"+ ShareConstants.CONNECTED;
                sendMsg(msg);

            }while (!isFinish);

        } catch (IOException e) {
            //LggUtils.d("startServer error: "+e.toString());
            Log.d(TAG, "zsr startServer error: "+e.toString());
            e.printStackTrace();

        }
    }


    @Override
    public void onResponse(final DataHandle handler, final String msg) {
        if (mResponseListener != null){
            mResponseListener.onResponse(msg);
        }
        mServerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (DataHandle dataHandle : mClientHandles) {
                    //跳过自身
                    if (dataHandle == handler){
                        continue;
                    }
                    dataHandle.sendMsg(msg);
                }
            }
        });

    }

    @Override
    public void disConnect(DataHandle handle) {
        if (mResponseListener != null){
            mResponseListener.onClientDisconnect(mClientSocket);
        }
        mClientHandles.remove(handle);
        if (mResponseListener != null){
            mResponseListener.clientCount(mClientHandles.size() );
        }
    }

    /**
     * 客户端的自己发送
     * @param msg
     */
    public void sendMsg(String msg) {
        if (mDataHandle != null){
            mDataHandle.sendMsg(msg);
        }
    }
    public void sendServerData(){

    }

    /**
     * 服务器的信息广播出去
     * @param msg
     */
    public synchronized void sendBroMsg(String msg){

        for (DataHandle handle : mClientHandles) {
            handle.sendMsg(msg);
        }
    }




    public void addResponseListener(BaseListener listener){
        mResponseListener = (TcpServerListener) listener;
    }


}
