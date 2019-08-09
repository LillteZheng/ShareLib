package com.zhengsr.drawlib.trans;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhengsr.drawlib.action.BaseAction;
import com.zhengsr.drawlib.action.EraseAction;
import com.zhengsr.drawlib.action.PenAction;
import com.zhengsr.drawlib.bean.TransBean;
import com.zhengsr.drawlib.callback.TransListener;
import com.zhengsr.drawlib.data.Command;
import com.zhengsr.drawlib.type.DrawActionType;
import com.zhengsr.drawlib.type.EventType;
import com.zhengsr.drawlib.type.PenType;
import com.zhengsr.drawlib.view.DrawSurface;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by zhengshaorui on 2019/7/19
 * Describe: 传输管理类
 */
public class TransManager {
    private TransListener mTransListener;
    private DrawSurface mView;
    static Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isResponsing = false;
    private TransManager(){}
    private static class Holder{
        static TransManager HODLER = new TransManager();
    }

    public static TransManager getInstance(){
        return Holder.HODLER;
    }


    public TransManager addTransListener(TransListener listener){
        mTransListener = listener;
        return this;
    }


    ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    /**
     * 传输响应
     * @param response
     */
    public synchronized void onTransResponse(final String response){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                parseTransBean(response);
            }
        });

    }

    private void actionDown(BaseAction action, TransBean bean){

        action.down(bean.x, bean.y, bean.pointId);
        mMultiAction.put(bean.pointId, action);
     //   mView.postDraw(mMultiAction, false);
        Command.getInstance().add(action);
    }

    private HashMap<Integer,BaseAction> mMultiAction = new LinkedHashMap<>();
    private void parseTransBean(String response){

        TransBean bean = JSON.parseObject(response,TransBean.class);
        DrawActionType tranType = DrawActionType.values()[bean.drawaAtionType];
        EventType eventype = EventType.values()[bean.eventType];
        BaseAction penAction;
        switch (eventype){
            case DOWN:
                isResponsing = true;
                if (DrawActionType.PEN == tranType){
                    PenType penType = PenType.values()[bean.penType];
                    if (penType == PenType.PEN) {
                        BaseAction action = new PenAction();
                        action.setPenSize(bean.paintSize);
                        action.setPenColor(bean.penColor);
                        actionDown(action,bean);
                    }else if(penType == PenType.LIGHT_PEN){
                        BaseAction action = new PenAction();
                        action.setPenSize(bean.paintSize);
                        action.setPenColor(bean.penColor);
                        actionDown(action,bean);
                    }
                }
                if (DrawActionType.ERASE == tranType){
                    EraseAction eraseAction = new EraseAction();
                    eraseAction.setEraseSize(bean.paintSize);
                    actionDown(eraseAction,bean);
                }

                break;
            case MOVE:
                isResponsing = true;
                penAction = mMultiAction.get(bean.pointId);
                if (penAction != null) {
                    penAction.move(bean.x, bean.y, bean.pointId);
                }
              //  mView.postDraw(mMultiAction, false);
                break;
            case POINT_UP:
                penAction = mMultiAction.get(bean.pointId);
                if (penAction != null) {
                    penAction.up(bean.x, bean.y, bean.pointId);
                   // penAction.draw(mView.getCanvas());
                }
                break;
            case UP:
                isResponsing = false;
                penAction = mMultiAction.get(bean.pointId);
                if (penAction != null) {
                    penAction.up(bean.x, bean.y, bean.pointId);
                   // penAction.draw(mView.getCanvas());
                }
              //  mView.postDraw(null, true);
                //每次都保存一下数据,方便分页
                mMultiAction.clear();

                break;
            default:break;

        }
    }




    public boolean isResponsing(){
        return isResponsing;
    }

    public void sendResponseConflict(){
        if (mTransListener != null){
            mTransListener.sendResponseConflict();
        }
    }



}
