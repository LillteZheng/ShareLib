package com.hht.sharelib.socket.udp;

import android.content.Context;

/**
 * @auther by zhengshaorui on 2019/7/22
 * describe: 用于分辨是udp发送还是接收类
 */
public class UdpManager {
    private static UdpSearcher mSearcher;
    private static UdpProvider mProvider;

    public static UdpSearcher startSearcher(){
        mSearcher = new UdpSearcher();
        return mSearcher;
    }

    public static UdpProvider startProvider(){
        mProvider = new UdpProvider();
        return mProvider;
    }


    public static void sendUdpBroadcast(){
        if (mSearcher != null) {
            mSearcher.sendUdpBroadcast();
        }
    }

    public static void stopSearcher(){
        if (mSearcher != null) {
            mSearcher.exit();
        }
    }

    public static void stopProvider(){
        if (mProvider != null) {
            mProvider.exit();
        }
    }
}
