package com.zhengsr.shareclient;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hht.sharelib.ShareRequest;
import com.hht.sharelib.ShareTrans;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.ClientListener;
import com.hht.sharelib.transtype.nio.entrance.client.NioClient;
import com.hht.sharelib.transtype.socket.udp.UdpManager;
import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;

import java.util.List;

public class MainActivity extends AppCompatActivity implements UdpSearcher.DeviceListener, ClientListener {
    private static final String TAG = "MainActivity";
    private ProgressDialog mDialog;
    private ShareRequest mShareTrans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mShareTrans = ShareTrans.get()
                .nio()
                .client()
                .searcheTime(2,this)
                .listener(this)
                .start();
    }

    public void search(View view) {
        mDialog = ProgressDialog.show(this, null, "正在搜索", true, true);
        mShareTrans.searchDevice();
        Log.d(TAG, "zsr 开始搜索");
    }

    @Override
    public void findDevice(List<DeviceInfo> devices) {
        mDialog.dismiss();
        if (devices != null && devices.size() > 0){
            DeviceInfo info = devices.get(0);
            mShareTrans.bindWidth(info.ip,this);
            Toast.makeText(this, info.toString(), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "未收到数据", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShareTrans.stop();
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
        mShareTrans.sendMsg("i am client");
    }
}
