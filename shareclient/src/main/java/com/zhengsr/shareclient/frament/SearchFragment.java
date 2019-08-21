package com.zhengsr.shareclient.frament;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hht.sharelib.TransServiceManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;
import com.zhengsr.shareclient.R;

import java.util.List;

/**
 * created by @author zhengshaorui on 2019/8/21
 * Describe:
 */
public class SearchFragment extends Fragment implements UdpSearcher.DeviceListener {

    private ProgressDialog dialog;

    public static SearchFragment newInstance() {

        Bundle args = new Bundle();

        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);


        view.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(getContext(), null, "正在搜索...");
                TransServiceManager.searchDevice(2000,SearchFragment.this);
            }
        });
        return view;
    }

    @Override
    public void findDevice(List<DeviceInfo> devices) {
        dialog.dismiss();
        DeviceInfo info = devices.get(0);
        if (info != null) {
            Toast.makeText(getContext(), "找到了: " + info.ip, Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content,ClientFragment.newInstance(info.ip))
                    .commit();
        }else {
            Toast.makeText(getContext(), "没有发现设备", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TransServiceManager.stopSearcher();
    }
}
