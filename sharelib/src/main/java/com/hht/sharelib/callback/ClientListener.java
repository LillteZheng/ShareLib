package com.hht.sharelib.callback;

import com.hht.sharelib.bean.DeviceInfo;

import java.net.Socket;

public interface ClientListener extends BaseListener {
    void serverConnected(DeviceInfo info);
    void serverDisconnect(DeviceInfo info);
    void serverConnectFail(String msg);
}
