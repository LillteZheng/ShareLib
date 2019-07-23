package com.hht.sharelib.callback;

import com.hht.sharelib.bean.Device;

import java.net.Socket;

public interface BaseListener {
    void onResponse(String msg);
}
