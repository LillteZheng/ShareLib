package com.hht.sharelib.socket.udp;

import android.os.Handler;
import android.os.Looper;

import com.hht.sharelib.socket.udp.client.UdpSearcher;
import com.hht.sharelib.socket.udp.server.UdpProvider;

/**
 * @auther by zhengshaorui on 2019/7/22
 * describe: 用于分辨是udp发送还是接收类
 */
public class UdpManager {
    private static UdpSearcher mSearcher;
    private static UdpProvider mProvider;


    public static UdpSearcher startSearcher(){
        mSearcher = UdpSearcher.create();
        return mSearcher;
    }

    public static UdpProvider startProvider(){
        mProvider = UdpProvider.create();
        return mProvider;
    }


    /**
     * timeout 单位秒
     * @param listener
     */
    public static void sendUdpBroadcast(UdpSearcher.DeviceListener listener){
        if (mSearcher != null) {
            mSearcher.sendUdpBroadcast(2,listener);
        }
    }

    public static void stopSearcher(){
        if (mSearcher != null) {
            mSearcher.stop();
        }
    }

    public static void stopProvider(){
        if (mProvider != null) {
            mProvider.stop();
        }
    }
}
