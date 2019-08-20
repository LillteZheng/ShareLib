package com.hht.sharelib.bean;

import java.io.Serializable;

public class DeviceInfo implements Serializable {
    public String ip;
    public int port;
    public String info;

    public DeviceInfo() {
    }

    public DeviceInfo(String ip, int port, String data) {
        this.ip = ip;
        this.port = port;
        this.info = data;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", info='" + info + '\'' +
                '}';
    }
}
