package com.hht.sharelib.socket.tcp;

import android.text.TextUtils;

import com.hht.sharelib.bean.DeviceInfo;
import com.hht.sharelib.CloseUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataHandle {
    private static final String TAG = "DataHandle";
    private WriteHandle mWriteHandle;
    private ReaderThread mReaderThread;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Socket mSocket;
    private DataListener mListener;
    public DataHandle(Socket socket, DataListener listener) {
        mSocket = socket;
        mListener = listener;
        try {
            mWriteHandle = new WriteHandle(socket.getOutputStream());
            mReaderThread = new ReaderThread(socket.getInputStream(), listener);
            mExecutorService.execute(mReaderThread);
            String ip = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            mListener.onConnect(new DeviceInfo(ip,port,null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exitBySelt(){
        exit();
        if (mListener != null){
            mListener.onSelfClosed(this);
        }
    }

    public void exit() {
        mWriteHandle.exit();
        mReaderThread.exit();
        mExecutorService.shutdownNow();
        CloseUtils.close(mSocket);


    }

    public void sendMsg(String msg) {
        if (mWriteHandle != null) {
            mWriteHandle.sendMsg(msg);
        }
    }

    public DeviceInfo getInfo(){
        if (mSocket != null) {
            String ip = mSocket.getInetAddress().getHostAddress();
            int port = mSocket.getPort();
            return new DeviceInfo(ip, port, null);
        }
        return null;
    }

    public interface DataListener {
        void onResponse(DataHandle handle, String msg);
        void disConnect(DataHandle handle);
        void onSelfClosed(DataHandle handle);
        void onConnect(DeviceInfo info);
    }

    class ReaderThread extends Thread {
        private BufferedReader br;
        private InputStream is;
        private boolean done = false;
        private DataListener listener;

        public ReaderThread(InputStream is, DataListener listener) {
            this.is = is;
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while (!done) {
                    String line = br.readLine();
                    if (listener != null) {
                        if (TextUtils.isEmpty(line)) {
                            listener.disConnect(DataHandle.this);
                            DataHandle.this.exitBySelt();
                            break;
                        } else {
                            listener.onResponse(DataHandle.this, line);
                        }
                    }
                }

            } catch (IOException e) {
               // e.printStackTrace();
                if (!done){
                    DataHandle.this.exitBySelt();
                }
            } finally {
                CloseUtils.close(is);
                CloseUtils.close(br);
            }
        }

        public void exit() {
            done = true;
            CloseUtils.close(is);
            CloseUtils.close(br);
        }
    }

    /**
     * 发送新开个线程，并发
     */
    class WriteHandle {
        private ExecutorService executorService;
        private boolean done;
        PrintStream ps;

        public WriteHandle(OutputStream outputStream) {
            executorService = Executors.newSingleThreadExecutor();
            ps = new PrintStream(outputStream);
        }

        public void sendMsg(String msg) {
            if (done) {
                return;
            }
            executorService.execute(new sendThread(msg));
        }

        public void exit() {
            done = true;
            executorService.shutdownNow();
            CloseUtils.close(ps);
        }

        class sendThread implements Runnable {

            String str;
            public sendThread(String str) {
                this.str = str;
            }
            @Override
            public void run() {
                if (done) {
                    return;
                }
                ps.println(str);

            }
        }
    }

}
