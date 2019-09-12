package com.hht.sharelib.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

public class PingUtils {

    /**
     * 是否ping通
     */
    public static boolean isPingOk(String ip) {
        try {
            return isPingOk(Runtime.getRuntime().exec("/system/bin/ping -s 16 -c 1 -w 1 -W 1 " + ip));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface PingListener{
        void isPingOk(boolean isCanPing);
    }

    public static boolean isPingOk(String ip, CountDownLatch latch) {
        try {
            boolean isping = isPingOk(Runtime.getRuntime().exec("/system/bin/ping -s 16 -c 1 -w 1 -W 1 " + ip));
            if (isping){
                //太快返回也不好
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
            return isping;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否ping通
     */
    private static boolean isPingOk(Process mProcess) {
        BufferedReader in = null;
        try {
            if (mProcess == null) {
                return false;
            }
            in = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("bytes from")) {
                    return true;
                }
            }
        } catch (IOException ignored) {
        } finally {
            try {
                assert in != null;
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            exitProcess(mProcess);
        }
        return false;
    }

    public static void exitProcess(Process mProcess) {
        if (mProcess != null) {
            try {
                int i = getPId(mProcess);
                if (i != 0) {
                    Process exitProcess = Runtime.getRuntime().exec("/system/bin/kill -2 " + i);
                    exitProcess.waitFor();
                    exitProcess.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                destroyProcess(mProcess);
            }
        }
    }

    private static void destroyProcess(Process process) {
        try {
            if (process != null) {
                process.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取Pid
    public static int getPId(Process process) {
        String paramProcess = process.toString();
        String pid = "0";
        try {
            int len = paramProcess.indexOf(",");
            if (len == -1) {
                len = paramProcess.indexOf("]");
            }
            pid = paramProcess.substring(paramProcess.indexOf("pid=") + 4, len);
            if (!TextUtils.isEmpty(pid)) {
                return Integer.parseInt(pid.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
