package com.hht.sharelib.callback;

import java.net.Socket;

public interface TcpServerListener extends BaseListener {
    void clientCount(int count);
    void onClientConnected(Socket socket);
    void onClientDisconnect(Socket socket);
}
