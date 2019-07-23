package com.hht.sharelib.socket.udp;


import android.os.Build;
import android.util.Log;

import com.hht.sharelib.NetConfig;
import com.hht.sharelib.ShareConstants;
import com.hht.sharelib.bean.NetBean;
import com.hht.sharelib.tools.CloseUtils;
import com.hht.sharelib.tools.ParseUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

/**
 * @auther by zhengshaorui on 2019/7/22
 * describe: udp 提供者，当接收到广播时，返回自身的ip和端口
 */
public class UdpProvider extends Thread {
    private static final String TAG = "UdpProvider";
    private boolean isFinish = false;
    private DatagramSocket socket;
    private NetBean mBean = NetConfig.getNetBean();

    public UdpProvider(){
        start();
    }
    @Override
    public void run() {
        super.run();
        try {
            socket = new DatagramSocket(mBean.udpPort);
            while(!isFinish) {
                byte[] bytes = new byte[128];
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                socket.receive(packet);

                String ip = packet.getAddress().getHostAddress();
                int port = packet.getPort();
                int length = packet.getLength();
                Log.d(TAG, "zsr UdpProvider: " + ip + "\tport: " + port);

                /**
                 * 发送消息给客户端
                 */
                String name = Build.MANUFACTURER;
                String device_model = Build.MODEL; // 设备型号 。

                String code = ParseUtils.buildWithSn(name+"/"+device_model);
                ByteBuffer buffer = ByteBuffer.wrap(bytes,0,length);
                int cmd = buffer.getInt();
                int resposePort = buffer.getInt();
                if (ShareConstants.CMD_BROAD == cmd) {

                    ByteBuffer responsebuf = ByteBuffer.allocate(256);
                    responsebuf.putInt(ShareConstants.CMD_BRO_RESPONSE);
                    responsebuf.putInt(mBean.tcpPort);
                    responsebuf.put(code.getBytes());
                    DatagramPacket receivePacket = new DatagramPacket(
                            responsebuf.array(),
                            responsebuf.position(),
                            packet.getAddress(),
                            resposePort
                    );
                    socket.send(receivePacket);
                }

            }
           // socket.close();


        } catch (Exception e) {
            //e.printStackTrace();
          //  LggUtils.d("udpprovider error "+e.toString());
            Log.d(TAG, "zsr udpprovider error "+e.toString());
        }finally {
            exit();
        }
    }
    public void exit(){
        isFinish = true;
        CloseUtils.close(socket);
    }
}
