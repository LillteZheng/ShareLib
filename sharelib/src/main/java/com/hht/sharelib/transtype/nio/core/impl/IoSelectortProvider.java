package com.hht.sharelib.transtype.nio.core.impl;

import com.hht.sharelib.transtype.nio.callback.IoProvider;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by @author zhengshaorui on 2019/8/20
 * Describe: ioProvider 的实现类，即 不同 selector 对应 读写
 */
public class IoSelectortProvider implements IoProvider {
    private Selector mReadSelector;
    private Selector mWriteSelector;

    //是否处于某个过程
    private AtomicBoolean mInRegInput = new AtomicBoolean(false);
    private AtomicBoolean mInRegOutput = new AtomicBoolean(false);
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    private final ExecutorService mInputHandlePool = Executors.newFixedThreadPool(4);
    private final ExecutorService mOutputHandlePool = Executors.newFixedThreadPool(4);

    private HashMap<SelectionKey,Runnable> mInputCallbackMap = new HashMap<>();
    private HashMap<SelectionKey,Runnable> mOutputCallbackMap = new HashMap<>();

    public IoSelectortProvider() throws IOException {
        mReadSelector = Selector.open();
        mWriteSelector = Selector.open();

        // 开始监听读写数据，注意这里是多线程模式,因为 IoSelectorProvider 已被设置成单利模式
        startRead();
        startWrite();

    }

    private void startWrite() {
        Thread thread = new Thread("Clink IoSelectorProvider WriteSelector Thread"){
            @Override
            public void run() {
                super.run();
                while(!isClosed.get()){
                    try {
                        if (mWriteSelector.select() == 0){
                            waitSelection(mInRegOutput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = mWriteSelector.selectedKeys();
                        //多线程处理方式，不直接遍历，避免数据阻塞
                        for (SelectionKey selectionKey : selectionKeys) {
                            //当前key是有效的
                            if (selectionKey.isValid()){
                                handleSelection(selectionKey,SelectionKey.OP_WRITE,mOutputCallbackMap,
                                        mOutputHandlePool);
                            }
                        }
                        selectionKeys.clear();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startRead() {
        Thread thread = new Thread("Clink IoSelectorProvider ReadSelector Thread"){
            @Override
            public void run() {
                super.run();
                while(!isClosed.get()){
                    try {
                        if (mReadSelector.select() == 0){
                            waitSelection(mInRegInput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = mReadSelector.selectedKeys();
                        //多线程处理方式，不直接遍历，避免数据阻塞
                        for (SelectionKey selectionKey : selectionKeys) {
                            //当前key是有效的
                            if (selectionKey.isValid()){
                                handleSelection(selectionKey,SelectionKey.OP_READ,mInputCallbackMap,mInputHandlePool);
                            }
                        }
                        selectionKeys.clear();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void handleSelection(SelectionKey key, int ops, HashMap<SelectionKey, Runnable> map,
                                 ExecutorService pool) {

        //先取消
        key.interestOps(key.interestOps() & ~ ops);

        Runnable runnable = null;
        runnable = map.get(key);
        if (runnable != null && !pool.isShutdown()) {
            pool.execute(runnable);
        }
    }

    /**
     * 等待是否注册成功
     * @param locker
     */
    private static void waitSelection(AtomicBoolean locker){
        synchronized (locker){
            if (locker.get()){
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean registerInput(SocketChannel channel, HandleInputCallback callback) {
        return registerSelection(channel,mReadSelector,SelectionKey.OP_READ,mInRegInput,
                mInputCallbackMap,callback) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandleOutputCallback callback) {
        return registerSelection(channel,mWriteSelector,SelectionKey.OP_WRITE,mInRegOutput,
                mOutputCallbackMap,callback) != null;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        unResgierSelection(channel,mReadSelector,mInputCallbackMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unResgierSelection(channel,mWriteSelector,mOutputCallbackMap);
    }


    /**
     * 取消某个 selector 在 channel 的注册
     */
    private void unResgierSelection(SocketChannel channel,Selector selector,
                                    HashMap<SelectionKey,Runnable> map){
        if (channel.isRegistered()){
            SelectionKey key = channel.keyFor(selector);
            if (key != null){
                /**
                 * 也可以用 key.interestOps(key.interestOps() & ~keyOps);
                 * 只是这里读写分离，所以用 key.cancel()也可以，如果是同个selector，
                 * 则需要区分一下读写
                 */
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }


    /**
     * 注册 selector 到 socketchannel
     */
    private SelectionKey registerSelection(SocketChannel channel, Selector selector, int registerOps,
                                           AtomicBoolean locker, HashMap<SelectionKey,Runnable> map,
                                           Runnable runnable){
        //多线程下，为了保证已经注册完了，用原子锁确定
        synchronized (locker){
            //设置锁定状态
            locker.set(true);
            try {
                //先唤醒，不让selector 处于 selector 状态
                selector.wakeup();
                SelectionKey key = null;
                //如果 channel 已经有注册过东西
                if (channel.isRegistered()){
                    //如果已经该 key 已经被注册过了
                    key = channel.keyFor(selector);
                    if (key != null){
                        //把这次的 selectorkey 添加进来
                        key.interestOps(key.interestOps() | registerOps);
                    }
                }
                //如果还没注册
                if (key == null){
                    key = channel.register(selector,registerOps);
                    //填充到 map 中
                    map.put(key,runnable);
                }
                return key;
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                //解除锁定，表示注册完成
                locker.set(false);
            }

        }

        return null;
    }


    @Override
    public void close() throws IOException {

    }
}
