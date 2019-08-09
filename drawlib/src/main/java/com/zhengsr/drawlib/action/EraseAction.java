package com.zhengsr.drawlib.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * created by zhengshaorui
 * time on 2019/7/11 
 */
public class EraseAction extends BaseAction {
    private Path mPath;
    private Paint mPaint;
    private PointF mPoint ;
    private int mPointId;
    private Paint mErasePaint;
    private int mEraseSize ;
    private int mOffert = 5;
    public EraseAction() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //使用这种方式，在截屏的时候，会出现背景被清掉的问题
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        mErasePaint = new Paint();
        mErasePaint.setAntiAlias(true);
        mErasePaint.setStyle(Paint.Style.STROKE);
        mErasePaint.setColor(Color.WHITE);

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
        if (mPointId == pointId) {
            /*float lastx = mPoint.x;
            float lasty = mPoint.y;
            mPath.quadTo(lastx, lasty, (x + lastx) / 2, (y + lasty) / 2);*/
            //这里并不需要贝塞尔曲线，不然会出现橡皮擦的小球在快速移动时，坐标不对导致残影问题
            mPoint.x = x;
            mPoint.y = y;
            mPath.lineTo(x,y);

        }
    }

    @Override
    public void up(float x, float y,int pointId) {
        if (mPointId == pointId) {
           // mPath.lineTo(x, y);
        }
        //不然会有残影留着，去掉橡皮擦圈圈
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


    }
    private Canvas mCanvas;
    public void drawErase(Canvas canvas){
        mCanvas = canvas;
        mCanvas.drawCircle(mPoint.x,mPoint.y, mEraseSize / 2 - mOffert,mErasePaint);

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
    public void setEraseSize(int size){
        this.mEraseSize = size;
        if (mPaint != null) {
            mPaint.setStrokeWidth(size);
        }
        mErasePaint.setPathEffect(new DashPathEffect(new float[] { 6, 6, 6, 6 }, 1));
    }
    @Override
    public void setbgBitmap(Bitmap bitmap){
      //  mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
    }

    public Rect getCurrentRect(Path path, Paint paint){
        RectF rf = new RectF();
        //拿到 path 的 bounds 边界
        path.computeBounds(rf,false);
        float pensize = paint.getStrokeWidth();
        int l = (int) (rf.left - pensize );
        int t = (int) (rf.top - pensize);
        int r = (int) (rf.right + pensize);
        int b = (int) (rf.bottom + pensize);
        return new Rect(l,t,r,b);
    }
}
