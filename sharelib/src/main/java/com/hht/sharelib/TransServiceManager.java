package com.hht.sharelib;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.hht.sharelib.bean.ConfigBean;
import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.transtype.socket.udp.UdpManager;
import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;
import com.hht.sharelib.type.KindType;
import com.hht.sharelib.type.TransType;
import com.hht.sharelib.utils.PingUtils;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: 统一管理接口
 */
public class TransServiceManager {
    private static final String TAG = "TransServiceManager";
    public static Handler HANDLER = new Handler(Looper.getMainLooper());
    private ConfigBean mConfigBean;
    private static TcpShareConfig mShareRequest;
    // ===============================================
    //              专门用于协同传输的接口
    // ===============================================

    private TransServiceManager(){
        mConfigBean = new ConfigBean();
    }
    public static TransServiceManager get(){
        mShareRequest = TcpShareConfig.create();
        return new TransServiceManager();
    }

    public TransServiceManager socket(){
        mConfigBean.transType = TransType.SOCKET;
        return this;
    }
    public TransServiceManager nio(){
        mConfigBean.transType = TransType.NIO;
        return this;
    }

    public TransServiceManager listener(BaseListener listener){
        mConfigBean.listener = listener;
        return this;
    }



    public TransServiceManager server(){
        mConfigBean.kindType = KindType.SERVER;
        return this;
    }
    /**
     * 绑定服务端
     * @param ip
     */
    public TransServiceManager client(String ip){
        mConfigBean.kindType = KindType.CLIENT;
        mConfigBean.ip = ip;
        return this;
    }


    /**
     * 启动服务
     */
    public void  start(){
        mShareRequest.start(mConfigBean);
    }

    /**
     * 只能用于服务端发送
     * @param msg
     */
    public static  void sendBroServerMsg(String msg){
        mShareRequest.sendBroMsg(msg);
    }
    public static  void sendBroServerFile(File file){
        mShareRequest.sendBroFile(file);
    }

    /**
     * 只能用于客户端发送
     * @param msg
     */
    public static void sendClientMsg(String msg){
        if (!TextUtils.isEmpty(msg)) {
            mShareRequest.sendMsg(msg);
        }
    }
    public static void sendClientFile(File file){
        if (file != null){
            mShareRequest.sendFile(file);
        }
    }

    public static void stop() {
        if (mShareRequest != null) {
            mShareRequest.stop();
        }
    }

    /**
     * UDP部分
     * @param time
     * @param listener
     */

    public static void searchDevice(int time, UdpSearcher.DeviceListener listener) {
        UdpManager.searchDevice(time, listener);
    }

    public static void startProvider(){
        UdpManager.startProvider();
    }
    public static void stopProvider(){
        UdpManager.stopProvider();
    }

    public static void stopSearcher(){
        UdpManager.stopSearcher();
    }


    /**
     *  是否能ping通，表示局域网
     */
    private static ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    public static void isCanPing(final String ip,final PingUtils.PingListener listener){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    CountDownLatch downLatch = new CountDownLatch(1);
                    final boolean isPing = PingUtils.isPingOk(ip,downLatch);
                    downLatch.await(2000, TimeUnit.MILLISECONDS);
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.isPingOk(isPing);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }





}
