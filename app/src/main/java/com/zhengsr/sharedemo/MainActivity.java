package com.zhengsr.sharedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hht.sharelib.TransServiceManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.TcpServerListener;

import java.io.File;

public class MainActivity extends AppCompatActivity implements TcpServerListener {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TransServiceManager.startProvider();
        TransServiceManager.get()
                .nio()
                .server()
                .listener(this)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TransServiceManager.stopProvider();
        TransServiceManager.stop();

    }

    @Override
    public void onResponse(String msg) {
        Log.d(TAG, "zsr onResponse: "+msg);
    }

    @Override
    public void onFileStart(byte type) {

    }

    @Override
    public void onFileSuccess(File file, byte type) {

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

    private int count;
    public void send(View view) {
        count ++;
        TransServiceManager.sendBroServerMsg("服务端数字: "+count);
    }
}
