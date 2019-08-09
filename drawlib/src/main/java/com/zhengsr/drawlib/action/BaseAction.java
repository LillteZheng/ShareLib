package com.zhengsr.drawlib.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * created by zhengshaorui
 * time on 2019/7/11
 */
public abstract class BaseAction {

    public abstract void down(float x,float y,int pointId);
    public abstract void move(float x,float y,int pointId);
    public abstract void up(float x,float y,int pointId);
    public abstract void draw(Canvas canvas);


    public void setPenColor(int pencolor) {
    }


    public void setPenSize(int penSize) {
    }

    public void setEraseSize(int size){

    }

    public void setbgBitmap(Bitmap bitmap){

    }

}
