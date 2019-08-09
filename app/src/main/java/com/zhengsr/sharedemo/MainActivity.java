package com.zhengsr.sharedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hht.sharelib.ShareManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.TcpServerListener;
import com.hht.sharelib.socket.udp.UdpManager;

public class MainActivity extends AppCompatActivity implements TcpServerListener {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShareManager.getInstance().startServer()
                    .addServerListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareManager.getInstance().stopServer();

    }

    @Override
    public void onResponse(String msg) {
        Log.d(TAG, "zsr onResponse: "+msg);
    }

    @Override
    public void onClientCount(int count) {
        Log.d(TAG, "zsr onClientCount: "+count);
    }

    @Override
    public void onClientConnected(DeviceInfo info) {
        Log.d(TAG, "zsr onClientConnected: "+info);
    }

    @Override
    public void onClientDisconnect(DeviceInfo info) {
        Log.d(TAG, "zsr onClientDisconnect: "+info);
    }

}
