package com.hht.sharelib.bean;

import com.hht.sharelib.callback.BaseListener;
import com.hht.sharelib.transtype.socket.udp.client.UdpSearcher;
import com.hht.sharelib.type.KindType;
import com.hht.sharelib.type.TransType;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe:
 */
public class ConfigBean {
    public TransType transType = TransType.NIO;
    public BaseListener listener ;
    public KindType kindType;
    public String ip;
}
