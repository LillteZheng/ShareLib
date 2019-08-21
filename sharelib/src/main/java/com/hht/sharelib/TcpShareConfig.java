package com.hht.sharelib;

import com.hht.sharelib.bean.ConfigBean;
import com.hht.sharelib.callback.TcpClientListener;
import com.hht.sharelib.callback.TcpServerListener;
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
 * Describe: TCP 的发送接收管理类
 */
public class TcpShareConfig {

    private ConfigBean mConfigBean;
    private Server mServer;
    private Client mClient;

   public static TcpShareConfig create(){
       return new TcpShareConfig();
   }

   private TcpShareConfig(){}

   public void start(ConfigBean configBean){
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
        switch (mConfigBean.transType){
            case NIO:
                if (mServer == null) {
                    mServer = NioServer.create();
                }
                break;
            case SOCKET:
                if (mServer == null) {
                    mServer = TcpServer.create();
                }
                break;
            case NETTY:
                break;
            default:break;
        }
        mServer.addResponseListener((TcpServerListener) mConfigBean.listener);
    }
    private void startClient() {
        switch (mConfigBean.transType){
            case NIO:
                if (mClient == null) {
                    mClient = new NioClient();
                }
                break;
            case SOCKET:
                if (mClient == null) {
                    mClient = TcpClient.create();
                }
                break;
            case NETTY:
                break;
            default:break;
        }
        mClient.bindWidth(mConfigBean.ip, (TcpClientListener) mConfigBean.listener);
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
            mServer.stop();
        }else if (mConfigBean.kindType == KindType.CLIENT){
            mClient.stop();
        }
    }


}
