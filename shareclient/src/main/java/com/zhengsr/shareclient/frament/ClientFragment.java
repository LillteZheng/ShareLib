package com.zhengsr.shareclient.frament;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hht.sharelib.TransServiceManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.TcpClientListener;
import com.zhengsr.shareclient.R;

import java.io.File;

/**
 * created by @author zhengshaorui on 2019/8/21
 * Describe:
 */
public class ClientFragment extends Fragment implements TcpClientListener {
    private static final String TAG = "ClientFragment";
    private ProgressDialog dialog;

    public static ClientFragment newInstance(String ip) {

        Bundle args = new Bundle();
        args.putString("ip",ip);
        ClientFragment fragment = new ClientFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client,container,false);



        TransServiceManager.get()
                .nio()
                .client(getArguments().getString("ip"))
                .listener(ClientFragment.this)
                .start();

        view.findViewById(R.id.send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransServiceManager.sendClientMsg("发送数据啦");
            }
        });
        return view;
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
    public void serverConnected(DeviceInfo info) {
        Log.d(TAG, "zsr serverConnected: "+info.toString());
    }

    @Override
    public void serverDisconnect(DeviceInfo info) {
        Log.d(TAG, "zsr serverDisconnect: "+info.toString());
    }

    @Override
    public void serverConnectFail(String msg) {
        Log.d(TAG, "zsr serverConnectFail: "+msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TransServiceManager.stop();
    }
}
