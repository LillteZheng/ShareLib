package com.hht.sharelib.tools;

public class ParseUtils {
    private static String PORT_HEADER = "回电端口:";
    private static String SN_HEADER = "我是:";

    public static String buildWithPort(int port){
        return PORT_HEADER+port;
    }
    public static int parsePort(String msg){
        if (msg.startsWith(PORT_HEADER)) {
            return Integer.parseInt(msg.substring(PORT_HEADER.length()));
        }
        return -1;
    }

    public static String buildWithSn(String sn){
        return SN_HEADER+sn;
    }

    public static String parseSn(String msg){
        if (msg.startsWith(SN_HEADER)) {
            return msg.substring(SN_HEADER.length());
        }
        return "null";
    }
}
