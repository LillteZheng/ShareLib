package com.hht.sharelib;

import com.hht.sharelib.bean.ConfigBean;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.ClientListener;
import com.hht.sharelib.callback.ServerListener;
import com.hht.sharelib.transtype.Client;
import com.hht.sharelib.transtype.Server;
import com.hht.sharelib.transtype.nio.entrance.client.NioClient;
import com.hht.sharelib.transtype.nio.entrance.server.NioServer;
import com.hht.sharelib.transtype.socket.tcp.client.TcpClient;
import com.hht.sharelib.transtype.socket.tcp.server.TcpServer;
import com.hht.sharelib.transtype.socket.udp.UdpManager;
import com.hht.sharelib.type.KindType;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe:
 */
public class ShareRequest {

    private ConfigBean mConfigBean;
    private Server mServer;
    private Client mClient;

    public ShareRequest(ConfigBean configBean) {
        mConfigBean = configBean;
        if (KindType.SERVER == configBean.kindType){
            startServer();
        }else if (KindType.CLIENT == configBean.kindType){
            startClient();
        }else {
            throw new RuntimeException("you need set kindType");
        }
    }


    private void startServer() {
        UdpManager.startProvider();
        switch (mConfigBean.transType){
            case NIO:
                mServer = NioServer.create();

                break;
            case SOCKET:
                mServer = TcpServer.create();
                break;
            case NETTY:
                break;
            default:break;
        }
        mServer.addResponseListener((ServerListener) mConfigBean.listener);
    }
    private void startClient() {
        UdpManager.startSearcher();
        switch (mConfigBean.transType){
            case NIO:
                mClient = new NioClient();
                break;
            case SOCKET:
                mClient = TcpClient.create();
                break;
            case NETTY:
                break;
            default:break;
        }
    }

    public void searchDevice(){
        UdpManager.sendUdpBroadcast(mConfigBean.searchTime,mConfigBean.deviceListener);
    }

    /**
     * 只能用于服务端发送
     * @param msg
     */
    public void sendBroMsg(String msg){
        mServer.sendBroMsg(msg);
    }
    /**
     * 只能用于客户端发送
     * @param msg
     */
    public void sendMsg(String msg){
        mClient.sendMsg(msg);
    }

    public void stop() {
        if (mConfigBean.kindType == KindType.SERVER) {
            UdpManager.stopProvider();
            mServer.stop();
        }else if (mConfigBean.kindType == KindType.CLIENT){
            mClient.stop();
            UdpManager.stopSearcher();
        }
    }

    /**
     * 绑定服务端
     * @param ip
     * @param listener
     */
    public void bindWidth(String ip, BaseListener listener) {
        mClient.bindWidth(ip, (ClientListener) listener);
    }
}
