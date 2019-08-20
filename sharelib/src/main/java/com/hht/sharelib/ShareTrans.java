package com.hht.sharelib;

import android.os.Handler;
import android.os.Looper;

import com.hht.sharelib.bean.ConfigBean;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;
import com.hht.sharelib.type.KindType;
import com.hht.sharelib.type.TransType;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: 统一管理接口
 */
public class ShareTrans {

    public static Handler HANDLER = new Handler(Looper.getMainLooper());
    private ConfigBean mConfigBean;

    // ===============================================
    //              专门用于协同传输的接口
    // ===============================================

    private ShareTrans(){
        mConfigBean = new ConfigBean();
    }
    public static ShareTrans get(){
        return new ShareTrans();
    }

    public ShareTrans socket(){
        mConfigBean.transType = TransType.SOCKET;
        return this;
    }
    public ShareTrans nio(){
        mConfigBean.transType = TransType.NIO;
        return this;
    }
    public ShareTrans netty(){
        mConfigBean.transType = TransType.NETTY;
        return this;
    }

    public ShareTrans listener(BaseListener listener){
        mConfigBean.listener = listener;
        return this;
    }

    public ShareTrans searcheTime(int time, UdpSearcher.DeviceListener listener) {
        mConfigBean.searchTime = time;
        mConfigBean.deviceListener = listener;
        return this;
    }

    public ShareTrans server(){
        mConfigBean.kindType = KindType.SERVER;
        return this;
    }

    public ShareTrans client(){
        mConfigBean.kindType = KindType.CLIENT;
        return this;
    }


    /**
     * 启动服务
     */
    public ShareRequest start(){
        return new ShareRequest(mConfigBean);
    }






}
