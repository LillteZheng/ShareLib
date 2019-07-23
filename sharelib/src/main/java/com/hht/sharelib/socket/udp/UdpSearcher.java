package com.hht.sharelib.socket.udp;

import android.util.Log;

import com.hht.sharelib.NetConfig;
import com.hht.sharelib.ShareConstants;
import com.hht.sharelib.bean.Device;
import com.hht.sharelib.bean.NetBean;
import com.hht.sharelib.tools.ParseUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @auther by zhengshaorui on 2019/7/22
 * describe: UDP 搜索，发送udp广播，并监听广播端口，如果有数据返回，则表示拿到服务端的信息
 */
public class UdpSearcher  {
    private static final String TAG = "UdpSearcher";
    private BroListener mBroListener;
    private List<Device> mDevices = new ArrayList<>();
    private ExecutorService mExecutorService;
    private CountDownLatch mCountDownLatch;
    private DeviceListener mDeviceListener;

    public UdpSearcher(){

    }
    public UdpSearcher addDeviceListener(DeviceListener listener){
        mDeviceListener = listener;
        mExecutorService =  Executors.newFixedThreadPool(2);
        mBroListener = new BroListener(listener);
        mExecutorService.execute(mBroListener);
        return this;
    }



    /**
     * 发送广播
     */
    public void sendUdpBroadcast() {
        final NetBean bean = NetConfig.getNetBean();

        //先清掉
        mDevices.clear();
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //广播端口由系统指定
                    DatagramSocket socket = new DatagramSocket();
                    //让它支持udp广播
                    socket.setBroadcast(true);
                    ByteBuffer buffer = ByteBuffer.allocate(128);
                    buffer.putInt(ShareConstants.CMD_BROAD);
                    buffer.putInt(bean.broadcastPort);
                    DatagramPacket packet = new DatagramPacket(
                            buffer.array(),
                            buffer.position(),
                            InetAddress.getByName(bean.broadcastIp),
                            bean.udpPort
                    );
                    socket.send(packet);
                    socket.close();
                  //  LggUtils.d("发送广播: "+bean.toString());
                    //需要放在线程
                    try {
                        mCountDownLatch = new CountDownLatch(1);
                        mCountDownLatch.await(2, TimeUnit.SECONDS);
                        if (mDeviceListener != null){
                            ShareConstants.HANDLE.post(new Runnable() {
                                @Override
                                public void run() {
                                    mDeviceListener.findDevice(mDevices);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public void exit(){
        if (mBroListener != null){
            mBroListener.exit();
        }
        if (mExecutorService != null){
            mExecutorService.shutdownNow();
        }
    }


    class BroListener extends Thread{
        private boolean isFinish = false;
        DatagramSocket socket = null;
        DeviceListener listener;
        NetBean bean = NetConfig.getNetBean();
        public BroListener(DeviceListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();

            try {
                socket = new DatagramSocket(bean.broadcastPort);
                mDevices.clear();
                while (!isFinish) {
                    //只要接受一些简单数值，所以可以不用那么大
                    byte[] bytes = new byte[256];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);

                    String ip = packet.getAddress().getHostAddress();
                    int length = packet.getLength();

                    ByteBuffer buffer = ByteBuffer.wrap(bytes,0,length);
                    int cmd = buffer.getInt();
                    int tcpPort = buffer.getInt();
                    int pos = buffer.position();
                    String msg = new String(bytes,pos,length - pos);
                    Log.d(TAG, "zsr 接收到服务器: " + ip + " " + tcpPort + " " + msg);
                    if (ShareConstants.CMD_BRO_RESPONSE == cmd) {
                        Device device = new Device(ip, tcpPort, ParseUtils.parseSn(msg));
                        mDevices.add(device);
                    }
                    mCountDownLatch.countDown();

                }

            } catch (Exception e) {
             //   e.printStackTrace();
            }finally {
                exit();
            }
        }
        public void exit(){
            if (socket != null){
                socket.close();
                socket = null;
            }
            isFinish = true;
        }
    }

    public interface DeviceListener{
        void findDevice(List<Device> devices);
    }


}
