package com.hht.sharelib.callback;

import java.net.Socket;

public interface TcpClientListener extends BaseListener {
    void connectServer(Socket socket);
    void serverDisconnect(Socket socket);
}
