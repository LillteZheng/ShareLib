package com.hht.sharelib.socket.udp.client;

import com.hht.sharelib.ShareManager;
import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.socket.UDPConstants;
import com.hht.sharelib.CloseUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: UDP 搜索，发送udp广播，并监听广播端口，如果有数据返回，则表示拿到服务端的信息
 */
public class UdpSearcher {
    private static final String TAG = "UdpSearcher";
    private final ExecutorService mSearchExecutorService;
    private ResponseListener mResponseListener;
    public static UdpSearcher create(){
        return new UdpSearcher();
    }

    private UdpSearcher(){
        mSearchExecutorService = Executors.newSingleThreadExecutor();
    }


    public void stop(){
        if (mSearchExecutorService != null){
            mSearchExecutorService.shutdownNow();
        }
        if (mResponseListener != null){
            mResponseListener.exit();
        }
    }

    /**
     * 发送广播
     * @param timeout 超时时间
     * @param listener
     */
    public void sendUdpBroadcast(final int timeout, final DeviceListener listener){
        mSearchExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    CountDownLatch downLatch = new CountDownLatch(1);
                    mResponseListener = listener(downLatch);
                    sendBroadcast();
                    downLatch.await(timeout, TimeUnit.SECONDS);
                    //切换到主线程
                    ShareManager.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.findDevice(mResponseListener.getDeviceInfos());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 启动线程配置
     * @param receiveDownLatch
     * @return
     * @throws InterruptedException
     */
    private ResponseListener listener(CountDownLatch receiveDownLatch) throws InterruptedException {
        /**
         * 受系统cpu和线程异步启动的影响，这压力需要线程启动完毕了，才能继续其他的操作
         */
        CountDownLatch startDownLatch = new CountDownLatch(1);
        ResponseListener listener = new ResponseListener(UDPConstants.PORT_CLIENT_RESPONSE, startDownLatch, receiveDownLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    /**
     * 监听服务端返回来的数据
     */
    class ResponseListener extends Thread{
        private int port;
        private DatagramSocket ds;
        private boolean done = false;
        private byte[] bytes = new byte[10];
        private ByteBuffer buffer;
        private List<DeviceInfo> deviceInfos = new ArrayList<>();
        private CountDownLatch startDownLatch,receiveDownLatch;
        public ResponseListener(int port,CountDownLatch startDownLatch,CountDownLatch receiveDownLatch) {
            this.port = port;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            super.run();
            //通知线程已启动
            startDownLatch.countDown();
            try {
                ds = new DatagramSocket(port);
                DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
                deviceInfos.clear();
                while (!done){
                    ds.receive(packet);
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    buffer = ByteBuffer.wrap(bytes);
                    int cmd = buffer.getInt();
                    int tcpPort = buffer.getInt();
                    if (UDPConstants.RESPONSE == cmd && tcpPort > 0){
                        DeviceInfo info = new DeviceInfo(ip,tcpPort,"server");
                        deviceInfos.add(info);
                    }
                    //成功接受到一份
                    receiveDownLatch.countDown();
                }
            }catch (Exception e){

            }finally {
                CloseUtils.close(ds);
            }
        }
        public void exit(){
            done = true;
            CloseUtils.close(ds);
        }

        public List<DeviceInfo> getDeviceInfos() {
            exit();
            return deviceInfos;
        }
    }
    
    /**
     * 发送udp广播
     * @throws SocketException
     */
    private void sendBroadcast() throws IOException {

        //创建 DatagramSocket 端口由系统指定
        DatagramSocket ds = new DatagramSocket();
        /**
         * 创建数据包
         */
        ByteBuffer buffer = ByteBuffer.allocate(10);
       // buffer.put(UDPConstants.HEADER);
        buffer.putInt(UDPConstants.REQUEST);
        buffer.putInt(UDPConstants.PORT_CLIENT_RESPONSE);
        DatagramPacket packet = new DatagramPacket(
                buffer.array(),
                buffer.position(),
                InetAddress.getByName(UDPConstants.BROADCAST_IP),
                UDPConstants.PORT_SERVER
        );
        ds.send(packet);
        ds.close();
    }

    public interface DeviceListener{
        void findDevice(List<DeviceInfo> devices);
    }

}
