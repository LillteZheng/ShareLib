package com.hht.sharelib.transtype.nio.entrance.client;

import android.text.TextUtils;
import android.util.Log;

import com.hht.sharelib.transtype.nio.packet.ReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.FileReceivePacket;
import com.hht.sharelib.transtype.nio.packet.box.FileSendPacket;
import com.hht.sharelib.transtype.nio.packet.box.StringReceivePacket;
import com.hht.sharelib.utils.CloseUtils;
import com.hht.sharelib.TransServiceManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.callback.TcpClientListener;
import com.hht.sharelib.transtype.Client;
import com.hht.sharelib.transtype.nio.IoContext;
import com.hht.sharelib.transtype.nio.core.Connector;
import com.hht.sharelib.transtype.nio.core.impl.IoSelectortProvider;
import com.hht.sharelib.transtype.socket.TCPConstants;
import com.hht.sharelib.utils.Foo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: NIO客户端
 */
public class NioClient extends Connector implements Client {
    private static final String TAG = "NioClient";
    private static final String CLIENT = "client";
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private TcpClientListener mResponseListener;
    private DeviceInfo mInfo;
    private String mFileName;
    @Override
    public void bindWidth(final String ip, final TcpClientListener listener){
        mResponseListener = listener;
        try {
            IoContext.setUp()
                    .ioProvider(new IoSelectortProvider())
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "zsr bindWidth error  "+e.getMessage());
        }

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketChannel socket = SocketChannel.open();
                    socket.connect(new InetSocketAddress(InetAddress.getByName(ip),TCPConstants.PORT_SERVER));
                    setUp(socket);
                    //表示连接成功
                    connectSuccess(socket);
                } catch (final IOException e) {
                    //e.printStackTrace();
                    TransServiceManager.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.serverConnectFail(e.toString());
                        }
                    });
                }
            }
        });
    }

    private void connectSuccess(final SocketChannel socket) {
        if (mResponseListener != null){
            TransServiceManager.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String ip = socket.socket().getInetAddress().getHostAddress();
                    int port = socket.socket().getPort();
                    mInfo = new DeviceInfo();
                    mInfo.ip = ip;
                    mInfo.port = port;
                    mInfo.info = "server connect success";
                    mResponseListener.serverConnected(mInfo);
                }
            });
        }
    }


    @Override
    public void stop(){
        IoContext.close();
        CloseUtils.close(this);
        //把发送过来的缓存文件夹去掉
        Foo.deleteFolder(CLIENT);
    }

    @Override
    public void sendMsg(String msg) {
        send(msg);
    }
    @Override
    public void sendFile(File file) {
        FileSendPacket packet = new FileSendPacket(file);
        //提醒对方要开始接收了
        sendMsg(Foo.FILE_START+file.getName());
        sendPacket(packet);
        if (mResponseListener != null){
            Foo.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    mResponseListener.onFileStart(Foo.TYPE_TRANS);
                }
            });
        }
    }

    @Override
    protected File createNewReceiveFile() {
        //用temp表示临时标量
        if (!TextUtils.isEmpty(mFileName)){
            mFileName = ".tmp."+mFileName.substring(mFileName.lastIndexOf(".")+1);
            File file = Foo.createNewFile(CLIENT, mFileName);
            mFileName = null;
            return file;
        }
        return Foo.createNewFile(CLIENT,".test.tmp");
    }

    @Override
    public void onChannelClosed(final SocketChannel channel) {
        super.onChannelClosed(channel);
        if (mResponseListener != null){
            TransServiceManager.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    mInfo.info = "server disconnect";
                    mResponseListener.serverDisconnect(mInfo);
                }
            });
        }
    }

    @Override
    protected void onReceivePacket(ReceivePacket packet) {
        super.onReceivePacket(packet);
        if (packet instanceof StringReceivePacket){
            if (mResponseListener != null){
                String str = ((StringReceivePacket) packet).entity();
                //收到文件接收完毕标志位,提示上层
                if (str.equals(Foo.FILE_END)){
                    Foo.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            mResponseListener.onFileSuccess(null,Foo.TYPE_TRANS);
                        }
                    });
                    return;
                }else if (str.startsWith(Foo.FILE_START)){
                    //提示接收开始
                    Foo.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                           mResponseListener.onFileStart(Foo.TYPE_ACK);
                        }
                    });
                    String[] s  = str.split(" ");
                    if (s.length > 1){
                        mFileName = s[1];
                    }else{
                        mFileName = "test.png";
                    }

                    return;
                }
                mResponseListener.onResponse(str);
            }
        }
        if (packet instanceof FileReceivePacket){
            final File file = ((FileReceivePacket) packet).entity();
            if (mResponseListener != null) {
                Foo.HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        //此时已经接收到客户端发来文件，应该提示接收完成
                        mResponseListener.onFileSuccess(file,Foo.TYPE_ACK);
                    }
                });
            }
            //发送一个应答信号过去，提示服务端已经接收到了文件
            sendMsg(Foo.FILE_END);
        }
    }
}
