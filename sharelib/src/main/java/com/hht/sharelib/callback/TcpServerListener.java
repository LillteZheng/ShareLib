package com.hht.sharelib.callback;

import com.hht.sharelib.bean.DeviceInfo;

import java.net.Socket;

public interface TcpServerListener extends BaseListener {
    void onClientCount(int count);
    void onClientConnected(DeviceInfo info);
    void onClientDisconnect(DeviceInfo info);
}
