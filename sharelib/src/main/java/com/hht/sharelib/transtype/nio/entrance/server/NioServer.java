package com.hht.sharelib.transtype.nio.entrance.server;

import android.util.Log;

import com.hht.sharelib.transtype.nio.packet.ReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.FileReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.FileSendPacket;
import com.hht.sharelib.transtype.nio.packet.box.StringReceivePacket;
import com.hht.sharelib.utils.CloseUtils;
import com.hht.sharelib.TransServiceManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.TcpServerListener;
import com.hht.sharelib.transtype.Server;
import com.hht.sharelib.transtype.nio.IoContext;
import com.hht.sharelib.transtype.nio.core.impl.IoSelectortProvider;
import com.hht.sharelib.transtype.socket.TCPConstants;
import com.hht.sharelib.utils.Foo;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: Tcp服务端，充当中转站，客户端信息发来时，转发给其他客户端,监听，读写和转发需分开异步
 */
public class NioServer  implements NioDataHandle.DataListener,Server {
    private static final String TAG = "NioServer";
    private List<NioDataHandle> mNioDataHandles = new ArrayList<>();
    private TcpServerListener mResponseListener;
    //转发线程池，异步并发
    private final ExecutorService mForwardingThreadPoolExecutor;
    private ClientListener mClientListener;
    private Selector mSelector;
    ServerSocketChannel server;
    public static NioServer create(){
        try {
            IoContext.setUp()
                    .ioProvider(new IoSelectortProvider())
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new NioServer();
    }

    private NioServer(){
        mForwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
        start();

    }

    /**
     * 开始监听客户端
     */
    private void start(){
        try {
            mSelector = Selector.open();
            server = ServerSocketChannel.open();
            server.configureBlocking(false);

            server.socket().bind(new InetSocketAddress(TCPConstants.PORT_SERVER));
            server.register(mSelector,SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mClientListener = new ClientListener();
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
        synchronized (NioServer.this){
            for (NioDataHandle NioDataHandle : mNioDataHandles) {
                NioDataHandle.exit();
            }
            mNioDataHandles.clear();
        }
        mForwardingThreadPoolExecutor.shutdownNow();
        IoContext.close();
        CloseUtils.close(server);
        CloseUtils.close(mSelector);

    }

    @Override
    public void sendBroMsg(String msg) {
        synchronized (NioServer.class) {
            for (NioDataHandle NioDataHandle : mNioDataHandles) {
                NioDataHandle.send(msg);
            }
        }
    }

    @Override
    public void sendBroFile(File file) {
        if (mResponseListener != null) {
            Foo.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    mResponseListener.onFileStart(Foo.TYPE_TRANS);
                }
            });
        }
        //发送之后，也要提醒对方要开始接收了
        sendBroMsg(Foo.FILE_START);
        synchronized (NioServer.class) {
            for (NioDataHandle nioDataHandle : mNioDataHandles) {
                nioDataHandle.sendPacket(new FileSendPacket(file));
            }
        }
    }


    /**
     * 监听客户端
     */
    class ClientListener extends Thread{
        boolean done = false;

        public ClientListener() {
        }

        @Override
        public void run() {
            super.run();
            try {

                while (!done){
                    //等待循环，防止cpu 100% 空转的问题
                    if (mSelector.select() == 0){
                        if (done){
                            break;
                        }
                        continue;
                    }

                    //拿到准备就绪的事件
                    Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        if (done){
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        //如果是接入事件
                        if (key.isAcceptable()){
                            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                            //拿到客户端
                            SocketChannel socketChannel = channel.accept();

                            if (checkClientExisit(socketChannel)){
                                continue;
                            }

                            //客户端构建读写异步线程
                            NioDataHandle clientHandle = new NioDataHandle(socketChannel,NioServer.this);
                            // clientHandle.readToPrint();
                            //同步，把客户端添加进来
                            synchronized (NioServer.this){
                                mNioDataHandles.add(clientHandle);
                            }
                        }
                    }

                }
            }catch (Exception e){
               // e.printStackTrace();
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
    private boolean checkClientExisit(SocketChannel socket){
        String ip = socket.socket().getInetAddress().getHostAddress();
        for (NioDataHandle handle : mNioDataHandles) {
            DeviceInfo info = handle.getInfo();
            if (info.ip.equals(ip)){
                return true;
            }
        }
        return false;
    }


    @Override
    public void onResponse(final NioDataHandle handle, final ReceivePacket packet) {

        if (packet instanceof StringReceivePacket){
            final String str = ((StringReceivePacket) packet).entity();
            //接收到文件标志
            if (str.equals(Foo.FILE_END)){
                if (mResponseListener != null){
                    Foo.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            mResponseListener.onFileSuccess(null,Foo.TYPE_TRANS);
                        }
                    });

                }
                return;
            }else if (str.equals(Foo.FILE_START)){
                //提示接收开始
                Foo.HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        mResponseListener.onFileStart(Foo.TYPE_ACK);
                    }
                });
                return;
            }
            //先发给自身
            if (mResponseListener != null){
                mResponseListener.onResponse(str);
            }
        }

        if (packet instanceof FileReceivePacket){
            final File file = ((FileReceivePacket) packet).entity();
            if (mResponseListener != null){
                Foo.HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        //此时已经接收到客户端发来文件，应该提示接收完成
                        mResponseListener.onFileSuccess(file,Foo.TYPE_ACK);
                    }
                });
            }
            //发送一个应答信号过去，提示客户端已经接收到了文件
            sendBroMsg(Foo.FILE_END);
        }

        //转发给其他客户端
        mForwardingThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (NioServer.this){
                    for (NioDataHandle NioDataHandle : mNioDataHandles) {
                        //过滤自身
                        if (NioDataHandle == handle){
                            continue;
                        }
                        if (packet instanceof StringReceivePacket) {
                            final String str = ((StringReceivePacket) packet).entity();
                            NioDataHandle.send(str);
                        }
                        if (packet instanceof FileReceivePacket){
                            File file = ((FileReceivePacket) packet).entity();
                            NioDataHandle.sendPacket(new FileSendPacket(file));
                        }

                    }
                }
            }
        });





    }



    @Override
    public synchronized void onSelfClosed(final NioDataHandle handle) {
        mNioDataHandles.remove(handle);
        TransServiceManager.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (mResponseListener != null) {
                    DeviceInfo info = handle.getInfo();
                    info.info = "client disconnected";
                    mResponseListener.onClientDisconnect(info);
                    mResponseListener.onClientCount(mNioDataHandles.size());
                }
            }
        });
    }

    @Override
    public void onConnect(final DeviceInfo info) {
        TransServiceManager.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (mResponseListener != null) {
                    info.info = "client connected";
                    mResponseListener.onClientConnected(info);
                    if (mNioDataHandles.size() == 0){
                        mResponseListener.onClientCount(1);
                    }else {
                        mResponseListener.onClientCount(mNioDataHandles.size());
                    }
                }
            }
        });
    }

    @Override
    public void addResponseListener(TcpServerListener listener){
        mResponseListener = listener;
    }

}
