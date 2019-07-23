package com.hht.sharelib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @auther by zhengshaorui on 2019/7/22
 * describe: 线程管理
 */
public class ThreadManager {
    static ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    public static void execute(Runnable runnable){
        mExecutorService.execute(runnable);
    }
}
