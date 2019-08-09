package com.zhengsr.drawlib.action;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.zhengsr.drawlib.DrawConfig;

/**
 * created by zhengshaorui
 * time on 2019/7/11 
 */
public class PenLightAction extends BaseAction {
    private static final String TAG = "PenAction";

    private Path mPath;
    private Paint mPaint;
    private PointF mPoint ;
    private int mPointId;

    public PenLightAction() {
        initPaint();
    }
    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
    }


    @Override
    public void down(float x, float y, int pointId) {
        mPath = new Path();
        mPoint = new PointF();
        mPoint.x = x;
        mPoint.y = y;
        mPath.moveTo(x,y);
        mPath.lineTo(x,y);
        mPointId = pointId;
    }

    @Override
    public void move(float x, float y, int pointId) {
        // mPath.lineTo(x,y);
        if (mPointId == pointId) {
            float lastx = mPoint.x;
            float lasty = mPoint.y;
            mPath.quadTo(lastx, lasty, (x + lastx) / 2, (y + lasty) / 2);
            mPoint.x = x;
            mPoint.y = y;

        }
    }

    public Rect getCurrentRect(Path path,Paint paint){
        RectF rf = new RectF();
        //拿到 path 的 bounds 边界
        path.computeBounds(rf,false);
        float pensize = paint.getStrokeWidth();
        int l = (int) (rf.left - pensize);
        int t = (int) (rf.top - pensize);
        int r = (int) (rf.right + pensize);
        int b = (int) (rf.bottom + pensize);
        return new Rect(l,t,r,b);
    }

    @Override
    public void up(float x, float y,int pointId) {
        if (mPointId == pointId) {
         //   mPath.lineTo(x, y);

           // mPath.reset();
        }


    }

    @Override
    public void draw(Canvas canvas) {
        if (mPath != null && mPaint != null) {
            canvas.save();
            canvas.clipRect(getCurrentRect(mPath,mPaint));
            canvas.drawPath(mPath, mPaint);
            canvas.restore();
        }
    }

    @Override
    public void setPenColor(int color){
        super.setPenColor(color);
        if (mPaint != null) {
            mPaint.setColor(color);
            //需要放置在颜色之后，不然会被重置
            mPaint.setAlpha(DrawConfig.LIGHT_ALPHA);
        }
    }
    @Override
    public void setPenSize(int size){
        if (mPaint != null) {
            mPaint.setStrokeWidth(size);
        }
    }




}
