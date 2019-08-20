package com.hht.sharelib.transtype.nio.callback;

import com.hht.sharelib.transtype.nio.core.IoArgs;

import java.io.Closeable;
import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 贵方上层接收接口
 */
public interface Receiver extends Closeable {
    //单独抽出来，避免多次注册
    void setReceiveListener(IoArgs.IoArgsEventListener listener);
    boolean receiveAsync(IoArgs ioArgs) throws IOException;
}
