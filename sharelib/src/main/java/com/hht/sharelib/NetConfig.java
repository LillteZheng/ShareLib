package com.hht.sharelib;

import android.text.TextUtils;

import com.hht.sharelib.bean.NetBean;

public class NetConfig {
    private static NetBean mBean = new NetBean();
    public static NetConfig udpPort(int updPort){
        mBean.udpPort = updPort;
        return new NetConfig();
    }

    public NetConfig broadcastPort(int port){
        mBean.broadcastPort = port;
        return this;
    }

    public NetConfig tcpPort(int tcpPort){
        mBean.tcpPort = tcpPort;
        return this;
    }
    
    public NetConfig broadcastIp(String ip){
        mBean.broadcastIp = ip;
        return this;
    }
    public NetConfig build(){
        checkNull(mBean);
        return this;
    }

    private void checkNull(NetBean mUdpBean) {
        if (mUdpBean.udpPort == -1){
            throw new RuntimeException("udpPort cannot be null ");
        }
        if (mUdpBean.tcpPort == -1){
            throw new RuntimeException("tcpPort cannot be null ");
        }
        if (mUdpBean.broadcastPort == -1){
            throw new RuntimeException("broadcastPort cannot be null ");
        }
        if (TextUtils.isEmpty(mUdpBean.broadcastIp)){
            mUdpBean.broadcastIp = "255.255.255.255";
        }

    }

    public static NetBean getNetBean() {
        return mBean;
    }
}
