package com.hht.sharelib.transtype.socket.tcp;

import com.hht.sharelib.callback.ClientListener;
import com.hht.sharelib.callback.ServerListener;
import com.hht.sharelib.transtype.socket.tcp.client.TcpClient;
import com.hht.sharelib.transtype.socket.tcp.server.TcpServer;

/**
 * created by zhengshaorui on 2019/7/23
 * Describe: 用来传输数据的东西
 */
public class TcpManager {
    private static TcpServer mTcpServer;
    private static TcpClient mTcpClient;




    public static void  createServer(){
        mTcpServer = TcpServer.create();
    }

    public static void createClient(String ip,ClientListener listener){
        mTcpClient = TcpClient.create();
        mTcpClient.bindWidth(ip,listener);
    }

    public static void  addServerListener(ServerListener listener){
        if (mTcpServer != null){
            mTcpServer.addResponseListener(listener);
        }

    }

    public static void  addClientListener(ClientListener listener){
        if (mTcpClient != null){
            mTcpClient.addResponseListener(listener);
        }
    }


    public static void sendBroServerMsg(String msg){
        if (mTcpServer != null){
            mTcpServer.sendBroMsg(msg);
        }
    }

    public static void sendClientMsg(String msg){
        if (mTcpClient != null){
           mTcpClient.sendMsg(msg);
        }
    }

    public static void stopServer(){
        if (mTcpServer != null){
            mTcpServer.stop();
        }
    }

    public static void stopClient(){
        if (mTcpClient != null){
            mTcpClient.stop();
        }
    }
}
