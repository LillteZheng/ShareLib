package com.zhengsr.drawlib.bean;

/**
 * created by zhengshaorui on 2019/7/18
 * Describe: 用来协同白板之间的数据传输
 */
public class TransBean {
    public TransBean() {
    }

    //比如橡皮，画笔，清屏等动作
    public int drawaAtionType;
    public float x = 0;
    public float y = 0;
    public int eventType;
    public int pointId;
    public int penType;
    public int penColor;
    public int paintSize;



}
