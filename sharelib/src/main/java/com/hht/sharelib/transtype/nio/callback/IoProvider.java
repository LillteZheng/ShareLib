package com.hht.sharelib.transtype.nio.callback;

import java.io.Closeable;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: 是一个观察者，用来注册Socketchannel的输入和输出，并把要实现的
 * 放在 Runnbale 的 run 方法中，供外部实现
 */
public interface IoProvider extends Closeable{
    boolean registerInput(SocketChannel channel,HandleInputCallback callback);
    boolean registerOutput(SocketChannel channel,HandleOutputCallback callback);


    void unRegisterInput(SocketChannel channel);
    void unRegisterOutput(SocketChannel channel);

    /**
     * 输入实现类，继承 runnable，用于线程池调度
     */
    abstract class HandleInputCallback implements Runnable{
        @Override
        public void run() {
            canProviderInput();
        }

        public abstract void canProviderInput();
    }
    /**
     * 输出实现类，继承 runnable，用于线程池调度
     */
    abstract class HandleOutputCallback implements Runnable{
        Object attach;

        public <T> T getAttach() {
            return (T) attach;
        }

        public void setAttach(Object attach) {
            this.attach = attach;
        }

        @Override
        public void run() {
            canProviderOutput();
        }
        public abstract void canProviderOutput();
    }
}
