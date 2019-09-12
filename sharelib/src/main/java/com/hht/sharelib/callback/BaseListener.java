package com.hht.sharelib.callback;

import java.io.File;

public interface BaseListener {
    /**
     * 字符串的传输
     */
    void onResponse(String msg);

    /**
     * 文件传输开始标志位
     * @param type FOO TYPE_TRANS FOO TYPE_ACK
     */
    void onFileStart(byte type);

    /**
     * 文件传输成功
     *  @param type FOO TYPE_TRANS FOO TYPE_ACK
     */
    void onFileSuccess(File file,byte type);

    //文件传输取消和失败后面再加


}
