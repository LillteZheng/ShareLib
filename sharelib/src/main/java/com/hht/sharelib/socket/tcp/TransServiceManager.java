package com.hht.sharelib.socket.tcp;

import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.TcpClientListener;
import com.hht.sharelib.callback.TcpServerListener;

/**
 * created by zhengshaorui on 2019/7/23
 * Describe: 用来传输数据的东西
 */
public class TransServiceManager {
    private static boolean mUseSocket = false;
    private static TcpServer mTcpServer;
    private static TcpClient mTcpClient;

    public static TransServiceManager useSocket(){
        mUseSocket = true;
        return new TransServiceManager();
    }


    public TransServiceManager createServer(){
        if (mUseSocket){
            mTcpServer = TcpServer.create();
        }
        return this;
    }

    public TransServiceManager createClient(String ip){
        if (mUseSocket){
            mTcpClient = TcpClient.create(ip);
        }
        return this;
    }

    public TransServiceManager addServerListener(BaseListener listener){
        if (mTcpServer != null){
            mTcpServer.addResponseListener(listener);
        }
        return this;
    }

    public TransServiceManager addClientListener(BaseListener listener){
        if (mTcpClient != null){
            mTcpClient.addResponseListener(listener);
        }
        return this;
    }


    public static void sendBroServerMsg(String msg){
        if (mTcpServer != null){
            mTcpServer.sendBroMsg(msg);
        }
    }

    public static void sendClientMsg(String msg){
        if (mTcpClient != null){
           mTcpClient.sendData(msg);
        }
    }

    public static void stopServer(){
        if (mTcpServer != null){
            mTcpServer.exit();
        }
    }

    public static void stopClient(){
        if (mTcpClient != null){
            mTcpClient.exit();
        }
    }
}
