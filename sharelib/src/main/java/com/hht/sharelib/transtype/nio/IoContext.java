package com.hht.sharelib.transtype.nio;

import android.content.Context;

import com.hht.sharelib.transtype.nio.callback.IoProvider;

import java.io.Closeable;
import java.io.IOException;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: NIO 配置入口
 */
public class IoContext{
    private static IoContext INSTANCE;
    private IoProvider mIoProvider;

    public IoContext(IoProvider ioProvider){
        mIoProvider = ioProvider;
    }

    public static IoContext get() {
        return INSTANCE;
    }

    public IoProvider getIoProvider() {
        return mIoProvider;
    }

    public static ConfigIoProvider setUp(){
        return new ConfigIoProvider();
    }

    public static void close() {
        if (INSTANCE != null){
            INSTANCE.callClose();
        }
    }
    private void callClose(){
        try {
            mIoProvider.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ConfigIoProvider {
        IoProvider ioProvider;
        public ConfigIoProvider ioProvider(IoProvider provider){
            this.ioProvider = provider;
            return this;
        }

        public IoContext start(){
            INSTANCE = new IoContext(ioProvider);
            return INSTANCE;
        }
    }


}
