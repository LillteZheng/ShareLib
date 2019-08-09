package com.zhengsr.shareclient;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hht.sharelib.ShareManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.callback.TcpClientListener;
import com.hht.sharelib.socket.udp.UdpManager;
import com.hht.sharelib.socket.udp.client.UdpSearcher;

import java.util.List;

public class MainActivity extends AppCompatActivity implements UdpSearcher.DeviceListener, TcpClientListener {
    private static final String TAG = "MainActivity";
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShareManager.getInstance().startSearcher();
    }

    public void search(View view) {
        mDialog = ProgressDialog.show(this, null, "正在搜索", true, true);
        ShareManager.getInstance().sendUdpBroadcast(this);
        Log.d(TAG, "zsr 开始搜索");
    }

    @Override
    public void findDevice(List<DeviceInfo> devices) {
        mDialog.dismiss();
        if (devices != null && devices.size() > 0){
            DeviceInfo info = devices.get(0);
            ShareManager.getInstance().connectServer(info.ip,this);
            Toast.makeText(this, info.toString(), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "未收到数据", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UdpManager.stopSearcher();
    }

    @Override
    public void onResponse(String msg) {
        Log.d(TAG, "zsr onResponse: "+msg);
    }

    @Override
    public void serverConnected(DeviceInfo info) {
        Log.d(TAG, "zsr ServerConnected: "+info);
    }

    @Override
    public void serverDisconnect(DeviceInfo info) {
        Log.d(TAG, "zsr serverDisconnect: "+info);
    }

    @Override
    public void serverConnectFail(String msg) {
        Log.d(TAG, "zsr serverConnectFail: "+msg);
    }

    public void send(View view) {
        ShareManager.getInstance().sendSingleMsg("i am client");
    }
}
