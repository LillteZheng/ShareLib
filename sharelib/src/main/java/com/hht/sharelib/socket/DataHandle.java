package com.hht.sharelib.socket;

import android.text.TextUtils;

import com.hht.sharelib.tools.CloseUtils;

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
    private WriteHandle mWriteHandle;
    private ReaderThread mReaderThread;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    public DataHandle(Socket socket, DataReadListener listener) {
        try {
            mWriteHandle = new WriteHandle(socket.getOutputStream());
            mReaderThread = new ReaderThread(socket.getInputStream(), listener);
            mExecutorService.execute(mReaderThread);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void exit(){
        mWriteHandle.exit();
        mReaderThread.exit();
        mExecutorService.shutdownNow();

    }

    public void sendMsg(String msg){
        if (mWriteHandle != null) {
            mWriteHandle.sendMsg(msg);
        }
    }

    public interface DataReadListener {
        void onResponse(DataHandle handle, String msg);
        void disConnect(DataHandle handle);
    }

    class ReaderThread extends Thread{
        private BufferedReader br;
        private InputStream is;
        private boolean isFinish = false;
        private DataReadListener listener;
        public ReaderThread(InputStream is, DataReadListener listener) {
            this.is = is;
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();
            try {
                br = new BufferedReader(new InputStreamReader(is));
                do {
                    String line = br.readLine();
                    if (listener != null){
                        if (TextUtils.isEmpty(line)){
                            listener.disConnect(DataHandle.this);
                            break;
                        }else {
                            listener.onResponse(DataHandle.this,line);
                        }
                    }
                }while(!isFinish);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void exit(){
            CloseUtils.close(is);
            CloseUtils.close(br);
        }
    }

    /**
     * 发送新开个线程，并发
     */
    class WriteHandle{
        private ExecutorService executorService;
        private boolean isFinish;
        PrintStream ps;
        public WriteHandle(OutputStream outputStream) {
            executorService = Executors.newSingleThreadExecutor();
            ps = new PrintStream(outputStream);
        }

        public void sendMsg(String msg){
            executorService.execute(new sendThread(msg));
        }
        public void exit(){
            isFinish = true;
            executorService.shutdownNow();
            CloseUtils.close(ps);
        }

        class sendThread implements Runnable{

            String str;

            public sendThread(String str) {
                this.str = str;
            }

            @Override
            public void run() {
                if (isFinish){
                    return;
                }
                ps.println(str);

            }
        }
    }

}
