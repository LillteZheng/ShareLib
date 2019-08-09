package com.zhengsr.drawlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.FrameLayout;


import java.util.LinkedHashMap;

/**
 * created by zhengshaorui on 2019/7/11
 * Describe: 配置画板基本属性
 */
public class DrawConfig {
    //最大10页
    public static int MAX_PAGE = 10;
    //画笔默认红河
    public static int PEN_COLOR = Color.WHITE;
    //画笔默认大小
    public static int PEN_SMALL_SIZE = 6;
    public static int PEN_MID_SIZE = 12;
    public static int PEN_LARGE_SIZE = 20;

    public static int PEN_DEFAULT_SIZE = PEN_SMALL_SIZE;
    //橡皮擦默认大小
    public static int ERASE_SIZE = 100;
    //荧光笔默认透明度
    public static int LIGHT_ALPHA = 150;

    private static Context mContext;

    /**
     * 需要传入的是 applicationcontext
     * @param context
     */
    public static void setContext(Context context){
        mContext = context;
    }

    public static Context context(){
        return mContext;
    }




}
