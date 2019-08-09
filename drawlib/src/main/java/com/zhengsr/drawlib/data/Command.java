package com.zhengsr.drawlib.data;


import com.zhengsr.drawlib.action.BaseAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Command {
    private static final String TAG = "Command";
    /**
     * 专门给多指用的
     */
    private HashMap<Integer,BaseAction> mMultiAction = new LinkedHashMap<>();
    private List<BaseAction> mUndoActions = new ArrayList<>();
    private List<BaseAction> mRedoActions = new ArrayList<>();
    private Command(){}
    private static class Holder{
        static Command HODLER = new Command();
    }

    public static Command getInstance(){
        return Holder.HODLER;
    }


    public void add(BaseAction action){
        mUndoActions.add(action);
    }

    public void putAction(int pointId,BaseAction action){
        mMultiAction.put(pointId,action);
    }
    public BaseAction getAction(int pointId){
        return mMultiAction.get(pointId);
    }
    public void removeAction(int pointId){
        BaseAction action = mMultiAction.get(pointId);
        if (action != null){
            mMultiAction.remove(pointId);
        }
    }

    public void redo() {
        if (mRedoActions.size() > 0) {
            int index = mRedoActions.size() - 1;
            BaseAction action = mRedoActions.get(index);
            mUndoActions.add(action);
            mRedoActions.remove(index);
        }
    }
    public void undo(){
        if (mUndoActions.size() > 0) {
            int index = mUndoActions.size() - 1;
            BaseAction action = mUndoActions.get(index);
            mRedoActions.add(action);
            mUndoActions.remove(index);
        }

    }

    public boolean isCanRedo(){
        return mRedoActions.size() > 0;
    }

    public boolean isCanUndo(){
        return mUndoActions.size() > 0;
    }
    public void clear(){
        mUndoActions.clear();
        mRedoActions.clear();
        mMultiAction.clear();
    }
    public void clearMultiAction(){
       mMultiAction.clear();
    }

    public List<BaseAction> getActions() {
        return mUndoActions;
    }


    public int  getMutilsActionSize(){
        return mMultiAction.size();
    }


    public HashMap<Integer, BaseAction> getMultiAction() {
        return mMultiAction;
    }



}
