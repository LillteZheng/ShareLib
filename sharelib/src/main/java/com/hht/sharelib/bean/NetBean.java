package com.hht.sharelib.bean;

public class NetBean {
    public int broadcastPort = -1;
    public int udpPort = -1;
    public int tcpPort = -1;
    public String broadcastIp = null;


    @Override
    public String toString() {
        return "NetBean{" +
                "broadcastPort=" + broadcastPort +
                ", udpPort=" + udpPort +
                ", tcpPort=" + tcpPort +
                ", broadcastIp='" + broadcastIp + '\'' +
                '}';
    }
}
