package com.hht.sharelib;


import android.os.Handler;
import android.os.Looper;

public class ShareConstants {

    public static int CMD_BROAD = 0x1001;
    public static int CMD_BRO_RESPONSE = 0x1001;
    public static String CONNECTED = "connected";
    public static Handler HANDLE = new Handler(Looper.getMainLooper());

}
