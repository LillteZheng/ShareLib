package com.hht.sharelib;

import android.os.Handler;
import android.os.Looper;

import com.hht.sharelib.bean.ConfigBean;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.transtype.socket.udp.UdpManager;
import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;
import com.hht.sharelib.type.KindType;
import com.hht.sharelib.type.TransType;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: 统一管理接口
 */
public class TransServiceManager {

    public static Handler HANDLER = new Handler(Looper.getMainLooper());
    private ConfigBean mConfigBean;
    private static TcpShareConfig mShareRequest;
    // ===============================================
    //              专门用于协同传输的接口
    // ===============================================

    private TransServiceManager(){
        mConfigBean = new ConfigBean();
    }
    public static TransServiceManager get(){
        mShareRequest = TcpShareConfig.create();
        return new TransServiceManager();
    }

    public TransServiceManager socket(){
        mConfigBean.transType = TransType.SOCKET;
        return this;
    }
    public TransServiceManager nio(){
        mConfigBean.transType = TransType.NIO;
        return this;
    }

    public TransServiceManager listener(BaseListener listener){
        mConfigBean.listener = listener;
        return this;
    }



    public TransServiceManager server(){
        mConfigBean.kindType = KindType.SERVER;
        return this;
    }
    /**
     * 绑定服务端
     * @param ip
     */
    public TransServiceManager client(String ip){
        mConfigBean.kindType = KindType.CLIENT;
        mConfigBean.ip = ip;
        return this;
    }


    /**
     * 启动服务
     */
    public void  start(){
        mShareRequest.start(mConfigBean);
    }

    /**
     * 只能用于服务端发送
     * @param msg
     */
    public static  void sendBroServerMsg(String msg){
        mShareRequest.sendBroMsg(msg);
    }
    /**
     * 只能用于客户端发送
     * @param msg
     */
    public static void sendClientMsg(String msg){
        mShareRequest.sendMsg(msg);
    }

    public static void stop() {
        mShareRequest.stop();
    }

    /**
     * UDP部分
     * @param time
     * @param listener
     */

    public static void searchDevice(int time, UdpSearcher.DeviceListener listener) {
        UdpManager.searchDevice(time, listener);
    }

    public static void startProvider(){
        UdpManager.startProvider();
    }
    public static void stopProvider(){
        UdpManager.stopProvider();
    }

    public static void stopSearcher(){
        UdpManager.stopSearcher();
    }










}
