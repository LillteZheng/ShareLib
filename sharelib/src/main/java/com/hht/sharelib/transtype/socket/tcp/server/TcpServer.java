package com.hht.sharelib.transtype.socket.tcp.server;

import android.util.Log;

import com.hht.sharelib.ShareTrans;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.ClientListener;
import com.hht.sharelib.callback.ServerListener;
import com.hht.sharelib.transtype.Server;
import com.hht.sharelib.transtype.socket.TCPConstants;
import com.hht.sharelib.transtype.socket.tcp.DataHandle;
import com.hht.sharelib.CloseUtils;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: Tcp服务端，充当中转站，客户端信息发来时，转发给其他客户端,监听，读写和转发需分开异步
 */
public class TcpServer implements DataHandle.DataListener,Server {
    private static final String TAG = "TcpServer";
    private List<DataHandle> mDataHandles = new ArrayList<>();
    private ServerListener mResponseListener;
    //转发线程池，异步并发
    private final ExecutorService mForwardingThreadPoolExecutor;
    private ClientListener mClientListener;

    public static TcpServer create(){
        return new TcpServer();
    }
    
    private TcpServer(){
        mForwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
        start();

    }

    /**
     * 开始监听客户端
     */
    private void start(){
        mClientListener = new ClientListener(TCPConstants.PORT_SERVER);
        mClientListener.start();
    }

    /**
     * 停止
     */
    @Override
    public void stop(){
        if (mClientListener != null){
            mClientListener.exit();
        }
        synchronized (TcpServer.this){
            for (DataHandle dataHandle : mDataHandles) {
                dataHandle.exit();
            }
            mDataHandles.clear();
        }
        mForwardingThreadPoolExecutor.shutdownNow();

    }

    /**
     * 发送广播信息
     * @param msg
     */
    @Override
    public void sendBroMsg(String msg) {
        synchronized (TcpServer.this) {
            for (DataHandle dataHandle : mDataHandles) {
                dataHandle.sendMsg(msg);
            }
        }
    }



    /**
     * 监听客户端
     */
    class ClientListener extends Thread{
        int port;
        boolean done = false;
        ServerSocket server;
        public ClientListener(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                server = new ServerSocket(port);
                while (!done){
                    Socket client = server.accept();
                    if (checkClientExisit(client)){
                        continue;
                    }
                    DataHandle handle = new DataHandle(client,TcpServer.this);
                    //同步，添加进来
                    synchronized (TcpServer.this){
                        mDataHandles.add(handle);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "zsr 客户端连接异常：" + e.getMessage());
            }finally {
                exit();
            }
        }
        void exit(){
            done = true;
            CloseUtils.close(server);
        }
    }

    /**
     * 检测是否已经存在了，防止重复添加
     * @param socket
     * @return
     */
    private boolean checkClientExisit(Socket socket){
        String ip = socket.getInetAddress().getHostAddress();
        for (DataHandle handle : mDataHandles) {
            DeviceInfo info = handle.getInfo();
            if (info.ip.equals(ip)){
                return true;
            }
        }
        return false;
    }


    @Override
    public void onResponse(final DataHandle handle, final String msg) {

        //先发给自身
        if (mResponseListener != null){
            mResponseListener.onResponse(msg);
        }

        //转发给其他客户端
        mForwardingThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (TcpServer.this){
                    for (DataHandle dataHandle : mDataHandles) {
                        //过滤自身
                        if (dataHandle == handle){
                            continue;
                        }
                        //对其他客户端发送消息
                        dataHandle.sendMsg(msg);
                    }
                }
            }
        });

    }

    @Override
    public void disConnect(final DataHandle handle) {
        ShareTrans.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (mResponseListener != null) {
                    DeviceInfo info = handle.getInfo();
                    info.info = "client disconnected";
                    mResponseListener.onClientDisconnect(info);
                    mResponseListener.onClientCount(mDataHandles.size());
                }
            }
        });
    }

    @Override
    public synchronized void onSelfClosed(DataHandle handle) {
        mDataHandles.remove(handle);
    }

    @Override
    public void onConnect(final DeviceInfo info) {
        ShareTrans.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (mResponseListener != null) {
                    info.info = "client connected";
                    mResponseListener.onClientConnected(info);
                    mResponseListener.onClientCount(mDataHandles.size());
                }
            }
        });
    }

    @Override
    public void addResponseListener(ServerListener listener){
        mResponseListener = listener;
    }

}
