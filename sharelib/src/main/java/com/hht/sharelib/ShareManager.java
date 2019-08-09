package com.hht.sharelib;

import android.os.Handler;
import android.os.Looper;

import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.TcpClientListener;
import com.hht.sharelib.callback.TcpServerListener;
import com.hht.sharelib.socket.tcp.TcpManager;
import com.hht.sharelib.socket.tcp.client.TcpClient;
import com.hht.sharelib.socket.tcp.server.TcpServer;
import com.hht.sharelib.socket.udp.UdpManager;
import com.hht.sharelib.socket.udp.client.UdpSearcher;
import com.hht.sharelib.socket.udp.server.UdpProvider;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: 统一管理接口
 */
public class ShareManager {

    public static Handler HANDLER = new Handler(Looper.getMainLooper());
    private static class Holder{
        static ShareManager HODLER = new ShareManager();
    }


    public static ShareManager getInstance(){
        return Holder.HODLER;
    }

    private ShareManager(){}



    // ===============================================
    //              专门用于协同传输的接口
    // ===============================================


    /**
     * 启动服务
     */
    public ShareManager startServer(){
        UdpManager.startProvider();
        TcpManager.createServer();
        return this;
    }

    /**
     * 关闭服务
     */
    public void stopServer(){
        UdpManager.stopProvider();
        TcpManager.stopServer();
    }

    /**
     * 开始客户端
     * @return
     */
    public ShareManager startSearcher(){
        UdpManager.startSearcher();
        return this;
    }

    public ShareManager connectServer(String ip,TcpClientListener listener){
        TcpManager.createClient(ip,listener);
        return this;
    }

    /**
     * 关闭客户端
     */
    public void stopClient(){
       UdpManager.stopSearcher();
       TcpManager.stopClient();
    }

    /**
     * 监听
     * @param listener
     * @return
     */
    public ShareManager addServerListener(BaseListener listener){
        if (listener instanceof TcpServerListener){
            TcpManager.addServerListener((TcpServerListener) listener);
        }
        return this;
    }

    /**
     * 发送广播数据
     * @param msg
     * @return
     */
    public ShareManager sendBroMsg(String msg){
        TcpManager.sendBroServerMsg(msg);
        return this;
    }

    /**
     * 发送单条信息
     * @param msg
     * @return
     */
    public ShareManager sendSingleMsg(String msg){
        TcpManager.sendClientMsg(msg);
        return this;
    }

    public ShareManager sendUdpBroadcast(UdpSearcher.DeviceListener listener){
        UdpManager.sendUdpBroadcast(listener);
        return this;
    }




}
