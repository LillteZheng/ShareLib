package com.hht.sharelib.transtype.socket;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: udp 参数常亮
 */
public class UDPConstants {

    // 服务器固化UDP接收端口
    public static final int PORT_SERVER = 30201;
    // 客户端回送端口
    public static final int PORT_CLIENT_RESPONSE = 30202;
    // udp 广播
    public static final String BROADCAST_IP = "255.255.255.255";

    /**
     * 用来识别的命令
     */
    public static final byte HEADER = 7;
    public static final int REQUEST = 1;
    public static final int RESPONSE = 2;

}
