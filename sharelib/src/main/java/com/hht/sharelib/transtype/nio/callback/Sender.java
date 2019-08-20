package com.hht.sharelib.transtype.nio.callback;

import com.hht.sharelib.transtype.nio.core.IoArgs;

import java.io.Closeable;
import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 贵方上层发送接口
 */
public interface Sender extends Closeable {
    boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException;
}
