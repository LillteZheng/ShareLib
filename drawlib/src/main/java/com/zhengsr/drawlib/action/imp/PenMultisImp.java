package com.zhengsr.drawlib.action.imp;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.zhengsr.drawlib.action.BaseAction;
import com.zhengsr.drawlib.action.PenAction;
import com.zhengsr.drawlib.action.PenLightAction;
import com.zhengsr.drawlib.bean.TransBean;
import com.zhengsr.drawlib.data.Command;
import com.zhengsr.drawlib.trans.TransManager;
import com.zhengsr.drawlib.type.DrawActionType;
import com.zhengsr.drawlib.type.EventType;
import com.zhengsr.drawlib.type.PenType;

import java.util.HashMap;

/**
 * created by zhengshaorui
 * time on 2019/7/11
 */
public class PenMultisImp extends BaseImp {
    private static final String TAG = "PenMultisImp";

    private Canvas mCanvas;
    private HashMap<Integer,BaseAction> mMultiAction;
    private volatile int pointid;
    private float mLastx,mLasty;
    public PenMultisImp() {
        //用来记录操作步骤的类，用来还原
    }

    @Override
    public void setCanvas(Canvas canvas) {
        mCanvas = canvas;
        mMultiAction = Command.getInstance().getMultiAction();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (TransManager.getInstance().isResponsing()){
            TransManager.getInstance().sendResponseConflict();
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                setAction(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                setAction(event);
                break;
            case MotionEvent.ACTION_MOVE:

                int count = event.getPointerCount();

                for (int i = 0; i < count; i++) {
                    int pointerId = event.getPointerId(i);
                    if (pointerId != -1) {
                        float x = event.getX(i);
                        float y = event.getY(i);
                        float dx = Math.abs(x - mLastx);
                        float dy = Math.abs((y - mLasty));
                        if (dx >= mTouchSlop || dy >= mTouchSlop) {

                        }
                        BaseAction action = mMultiAction.get(pointerId);
                        if (action != null) {
                            action.move(x, y, pointerId);
                           TransBean bean = getTransBean(DrawActionType.PEN);

                            bean.pointId = pointerId;
                            bean.x = x;
                            bean.y = y;
                            bean.eventType = EventType.MOVE.ordinal();
                            sendTransData(bean);


                        }
                        mLastx = x;
                        mLasty = y;
                    }
                }

                draw(false);
                break;
            case MotionEvent.ACTION_POINTER_UP:

                pointid = event.getPointerId(event.getActionIndex());
                BaseAction baseAction = mMultiAction.get(pointid);
                if (baseAction != null) {
                    float x = event.getX(event.getActionIndex());
                    float y = event.getY(event.getActionIndex());
                    baseAction.up(x, y, pointid);
                    baseAction.draw(mCanvas);

                    TransBean upbean = getTransBean(DrawActionType.PEN);
                    upbean.pointId = pointid;
                    upbean.x = x;
                    upbean.y = y;
                    upbean.eventType = EventType.POINT_UP.ordinal();

                    sendTransData(upbean);

                }
                break;
            case MotionEvent.ACTION_UP:

                pointid = event.getPointerId(event.getActionIndex());
                BaseAction action = mMultiAction.get(pointid);
                if (action != null) {
                    float x = event.getX(event.getActionIndex());
                    float y = event.getY(event.getActionIndex());
                    action.up(x, y, pointid);
                    action.draw(mCanvas);


                    TransBean upbean = getTransBean(DrawActionType.PEN);
                    upbean.pointId = pointid;
                    upbean.x = x;
                    upbean.y = y;
                    upbean.eventType = EventType.UP.ordinal();
                    sendTransData(upbean);
                }

                draw(true);
                mMultiAction.clear();
                break;
            default:
                break;
        }


        return true;
    }


    /**
     * 每次down事件都是一个 action
     *
     * @param event
     * @return
     */
    private BaseAction setAction(MotionEvent event) {
        BaseAction action = null;
        if (getPenType() == PenType.PEN) {
            action = new PenAction();

        } else if (getPenType() == PenType.LIGHT_PEN) {
            action = new PenLightAction();
        }

        int pointerId = event.getPointerId(event.getActionIndex());
        int actionIndex = event.getActionIndex();
        float x = event.getX(actionIndex);
        float y = event.getY(actionIndex);
        action.setPenColor(getPenColor());
        action.setPenSize(getPenSize());
        action.down(x, y, pointerId);
        //添加action
        mMultiAction.put(pointerId, action);
        Command.getInstance().add(action);

        //协同白板要发送的数据
        TransBean bean = getTransBean(DrawActionType.PEN);
        mLastx = bean.x = x;
        mLasty = bean.y = y;
        bean.pointId = pointerId;
        bean.eventType  = EventType.DOWN.ordinal();
        sendTransData(bean);
        draw(false);

        return action;
    }





    private  void draw(boolean isRedraw) {
        /**
         * 接口数据传输，不能用线程，否则数据则塞和异步问题，会导致掉线问题
         */
        if (mDrawListener != null) {
            if (mMultiAction != null && mMultiAction.size() > 0) {

                mDrawListener.onAction(mMultiAction,isRedraw);
            }
        }


    }

}
