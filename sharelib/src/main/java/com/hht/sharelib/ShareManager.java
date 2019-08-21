package com.hht.sharelib;

import android.os.Handler;
import android.os.Looper;

import com.hht.sharelib.bean.ConfigBean;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.ClientListener;
import com.hht.sharelib.transtype.socket.udp.UdpManager;
import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;
import com.hht.sharelib.type.KindType;
import com.hht.sharelib.type.TransType;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: 统一管理接口
 */
public class ShareManager {

    public static Handler HANDLER = new Handler(Looper.getMainLooper());
    private ConfigBean mConfigBean;
    private static ShareRequest mShareRequest;
    // ===============================================
    //              专门用于协同传输的接口
    // ===============================================

    private ShareManager(){
        mConfigBean = new ConfigBean();
    }
    public static ShareManager get(){
        mShareRequest = ShareRequest.create();
        return new ShareManager();
    }

    public ShareManager socket(){
        mConfigBean.transType = TransType.SOCKET;
        return this;
    }
    public ShareManager nio(){
        mConfigBean.transType = TransType.NIO;
        return this;
    }

    public ShareManager listener(BaseListener listener){
        mConfigBean.listener = listener;
        return this;
    }

    public ShareManager searchTime(int time, UdpSearcher.DeviceListener listener) {
        mConfigBean.searchTime = time;
        mConfigBean.deviceListener = listener;
        return this;
    }

    public ShareManager server(){
        mConfigBean.kindType = KindType.SERVER;
        return this;
    }

    public ShareManager client(){
        mConfigBean.kindType = KindType.CLIENT;
        return this;
    }


    /**
     * 启动服务
     */
    public void  start(){
        mShareRequest.start(mConfigBean);
    }


    public void searchDevice(){
        UdpManager.sendUdpBroadcast(mConfigBean.searchTime,mConfigBean.deviceListener);
    }

    /**
     * 只能用于服务端发送
     * @param msg
     */
    public void sendBroMsg(String msg){
        mShareRequest.sendBroMsg(msg);
    }
    /**
     * 只能用于客户端发送
     * @param msg
     */
    public void sendMsg(String msg){
        mShareRequest.sendMsg(msg);
    }

    public void stop() {
        mShareRequest.stop();
    }

    /**
     * 绑定服务端
     * @param ip
     * @param listener
     */
    public void bindWidth(String ip, BaseListener listener) {
        mShareRequest.bindWidth(ip, (ClientListener) listener);
    }





}
