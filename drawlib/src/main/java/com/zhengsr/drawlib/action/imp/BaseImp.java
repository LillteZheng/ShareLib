package com.zhengsr.drawlib.action.imp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.zhengsr.drawlib.action.BaseAction;
import com.zhengsr.drawlib.bean.TransBean;
import com.zhengsr.drawlib.data.DrawDataSave;
import com.zhengsr.drawlib.type.DrawActionType;
import com.zhengsr.drawlib.type.PenType;

import java.util.HashMap;

/**
 * created by zhengshaorui
 * time on 2019/7/11
 * 用来设置白板页面的各种设置
 */
public abstract class BaseImp {
    //默认属性
    DrawDataSave mData;
    protected float mTouchSlop;
    private TransBean mTransBean;
    public BaseImp() {
        mData = DrawDataSave.getInstance();
    }

    public abstract void setCanvas(Canvas canvas);


    public int getEraseSize() {

        return mData.getEraseSize();
    }

    public void setContext(Context context){
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setEraseSize(int eraseSize) {
       mData.setEraseSize(eraseSize);
    }

    public int getPenSize() {
        return mData.getPenSize();
    }

    public void setPenSize(int penSize) {
        mData.setPenSize(penSize);
    }

    public int getPenColor() {
        return mData.getPenColor();
    }

    public void setPenColor(int penColor) {
        mData.setPenColor(penColor);
    }

    public Bitmap getBgBitmap() {
        return mData.getBgBitmap();
    }

    public void setBgBitmap(Bitmap bgBitmap) {
        mData.setBgBitmap(bgBitmap);
    }

    public  abstract boolean onTouchEvent(MotionEvent event);
    public interface DrawListener{
        void onAction(HashMap<Integer, BaseAction> actions, boolean isRedraw);
        void sendTrans(TransBean bean);
    }
    protected DrawListener mDrawListener;
    public void addDrawListener(DrawListener listener){
        mDrawListener = listener;
    }

    public PenType getPenType() {
        return mData.getPenType();
    }

    public void setPenType(PenType penType) {
        mData.setPenType(penType);
    }


    protected TransBean getTransBean(DrawActionType type){
        if (mTransBean == null) {
            mTransBean = new TransBean();
        }
        mTransBean.penColor = getPenColor();
        mTransBean.penType = getPenType().ordinal();
        mTransBean.drawaAtionType = type.ordinal();
        if (type == DrawActionType.PEN) {
            mTransBean.paintSize = getPenSize();
        }else if (type == DrawActionType.ERASE){
            mTransBean.paintSize = getEraseSize();
        }
        return mTransBean;
    }

    protected void sendTransData(final TransBean bean){

        if (mDrawListener != null){
            mDrawListener.sendTrans(bean);
        }
    }



}
