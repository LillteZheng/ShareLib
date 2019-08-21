package com.hht.sharelib.transtype;

import com.hht.sharelib.callback.TcpServerListener;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 定义服务端
 */
public interface Server {

    /**
     * 停止
     */
     void stop();
    /**
     * 发送广播信息
     * @param msg
     */
     void sendBroMsg(String msg);

    /**
     * 监听数据
     * @param listener
     */
     void addResponseListener(TcpServerListener listener);
}
