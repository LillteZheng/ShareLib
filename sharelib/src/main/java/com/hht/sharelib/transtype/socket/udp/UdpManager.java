package com.hht.sharelib.transtype.socket.udp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;
import com.hht.sharelib.transtype.socket.udp.server.UdpProvider;

/**
 * @auther by zhengshaorui on 2019/7/22
 * describe: 用于分辨是udp发送还是接收类
 */
public class UdpManager {
    private static final String TAG = "UdpManager";
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
    public static void searchDevice(int time, UdpSearcher.DeviceListener listener){
        if (mSearcher == null){
            startSearcher();
        }

        mSearcher.sendUdpBroadcast(time,listener);
    }

    public static void stopSearcher(){
        if (mSearcher != null) {
            mSearcher.stop();
            mSearcher = null;
        }
    }

    public static void stopProvider(){
        if (mProvider != null) {
            mProvider.stop();
            mProvider = null;
        }
    }
}
