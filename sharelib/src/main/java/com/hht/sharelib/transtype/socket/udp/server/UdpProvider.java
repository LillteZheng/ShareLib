package com.hht.sharelib.transtype.socket.udp.server;

import android.util.Log;

import com.hht.sharelib.transtype.socket.TCPConstants;
import com.hht.sharelib.transtype.socket.UDPConstants;
import com.hht.sharelib.CloseUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: udp 提供者，当接收到广播时，返回自身的ip和端口
 */
public class UdpProvider {
    private static final String TAG = "UdpProvider";
    private ResponseListener mResponseListener;
    private ExecutorService mExecutorService ;

    public static UdpProvider create(){
        return new UdpProvider();
    }

    private UdpProvider(){
        mExecutorService = Executors.newSingleThreadExecutor();
        mResponseListener = new ResponseListener(UDPConstants.PORT_SERVER);
        mExecutorService.execute(mResponseListener);
    }

    public void stop(){
        if (mResponseListener != null){
            mResponseListener.exit();
        }
        if (mExecutorService != null){
            mExecutorService.shutdownNow();
        }
    }


    class ResponseListener extends Thread{
        private int port;
        private DatagramSocket ds;
        private byte[] bytes = new byte[10];
        private boolean done;
        private ByteBuffer buffer;
        public ResponseListener(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                ds = new DatagramSocket(port);
                DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
                while (!done){
                    ds.receive(packet);
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();

                    buffer = ByteBuffer.wrap(bytes);
                    int cmd = buffer.getInt();
                    int responsePort = buffer.getInt();
                    Log.d(TAG, "zsr UdpProvider: "+ip+" "+port+" "+cmd+" "+responsePort);

                    if (UDPConstants.REQUEST == cmd && responsePort > 0){
                        /**
                         * 返回自身端口和数据
                         */
                        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
                      //  byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putInt(UDPConstants.RESPONSE);
                        byteBuffer.putInt(TCPConstants.PORT_SERVER);
                        DatagramPacket responsePacket = new DatagramPacket(
                                byteBuffer.array(),
                                byteBuffer.position(),
                                packet.getAddress(),
                                responsePort
                        );
                        ds.send(responsePacket);
                    }
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
    }
}
