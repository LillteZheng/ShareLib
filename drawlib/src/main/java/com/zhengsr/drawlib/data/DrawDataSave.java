package com.zhengsr.drawlib.data;

import android.graphics.Bitmap;

import com.zhengsr.drawlib.DrawConfig;
import com.zhengsr.drawlib.type.PenType;

public class DrawDataSave {
    private int eraseSize = DrawConfig.ERASE_SIZE;
    private int penSize = DrawConfig.PEN_DEFAULT_SIZE;
    private int penColor = DrawConfig.PEN_COLOR;
    private PenType penType = PenType.PEN;

    public PenType getPenType() {
        return penType;
    }

    public void setPenType(PenType penType) {
        this.penType = penType;
    }

    private Bitmap bgBitmap;

    private DrawDataSave(){}
    private static class Holder{
        static DrawDataSave data = new DrawDataSave();
    }
    public static DrawDataSave getInstance(){
        return Holder.data;
    }

    public int getEraseSize() {
        return eraseSize;
    }

    public void setEraseSize(int eraseSize) {
        this.eraseSize = eraseSize;
    }

    public int getPenSize() {
        return penSize;
    }

    public void setPenSize(int penSize) {
        this.penSize = penSize;
    }

    public int getPenColor() {
        return penColor;
    }

    public void setPenColor(int penColor) {
        this.penColor = penColor;
    }

    public Bitmap getBgBitmap() {
        return bgBitmap;
    }

    public void setBgBitmap(Bitmap bgBitmap) {
        this.bgBitmap = bgBitmap;
    }
}
