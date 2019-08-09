package com.zhengsr.drawlib.callback;


/**
 * created by zhengshaorui on 2019/7/19
 * Describe: 用来传输两个白板之间的数据
 */
public interface TransListener {
    void sendTransData(String drawMsg);
    //划线和回送冲突了
    void sendResponseConflict();
}
