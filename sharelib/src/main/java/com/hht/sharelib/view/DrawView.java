package com.hht.sharelib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hht.sharelib.bean.TransInfoBean;

import java.util.ArrayList;
import java.util.List;

public class  DrawView extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    private static final String TAG = "DrawView";
    private Path mPath;
    private Paint mPaint;
    private boolean isDrawing;
    private Bitmap mBackBitmap;
    private SurfaceHolder mHolder;
    private Paint mEraserPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap mBg;

    public DrawView(Context context) {
        this(context,null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);

        getHolder().addCallback(this);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBg = drawGrayLineBg(width,height);


    }

    public  Bitmap drawGrayLineBg(int width, int height) {
        Bitmap localBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(localBitmap);
        canvas.drawColor(Color.parseColor("#333b3e"));
        Paint localPaint = new Paint();
        localPaint.setAntiAlias(true);
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setStrokeWidth(2.0F);
        localPaint.setColor(Color.BLACK);
        localPaint.setAlpha(50);
        //缩略图20
        int space = 80;
        if(height<600){
            space = 10;
        }
        for (int i = 0; i < width / space; i++) {
            canvas.drawLine(i * space, 0, i * space, height, localPaint);
        }
        for (int j = 0; j < height / space + 1; j++) {
            canvas.drawLine(0, j * space, width, j * space, localPaint);
        }
        return localBitmap;
    }

    private void initTools() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(8);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);

        mEraserPaint = new Paint();
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setAlpha(0);
        mEraserPaint.setStrokeWidth(20);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setColor(Color.WHITE);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));



        mPath = new Path();
        mClearPath = new Path();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
            initTools();

    }

    private int mode;
    public void setFx(int i){
        mode = i;
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //阻止第一次进入黑屏的问题
        Canvas canvas = getHolder().lockCanvas();
       // mCanvas.drawBitmap(mBg,0,0,null);
        canvas.drawBitmap(mBitmap,0,0,null);
        getHolder().unlockCanvasAndPost(canvas);
        isDrawing = true;
        //new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false;
    }

    private float mPreX,mPreY;
    private Path mClearPath;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPreX = event.getX();
                mPreY = event.getY();
                if (mode == 2) {
                    mClearPath.moveTo(mPreX, mPreY);
                }else{

                mPath.moveTo(mPreX,mPreY);
                }
               // addPath(event);
                if (mListener != null){
                    mListener.down(mPreX,mPreY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                float dx = Math.abs(x - mPreX);
                float dy = Math.abs(y - mPreY);
                if (dx >2 || dy >2){ //有移动时才画
                    // 注意这里的mprex，虽然在down的时候，我们记录了mprex，但这里却是 move 的坐标
                    // 所以，这里的quadto，其实是在移动时坐标的中点
                    if (mode == 2) {
                        Canvas canvas = getHolder().lockCanvas();
                        if (canvas != null) {
                         //   mCanvas.drawBitmap(mBg,0,0,null);
                            mClearPath.quadTo(mPreX, mPreY, (x + mPreX) / 2, (y + mPreY) / 2);
                            mCanvas.drawPath(mPath, mPaint);
                            mCanvas.drawPath(mClearPath, mEraserPaint);
                            canvas.drawBitmap(mBitmap,0,0,null);

                            getHolder().unlockCanvasAndPost(canvas);
                        }


                    }else{
                            mPath.quadTo(mPreX,mPreY,(x + mPreX)/2,(y + mPreY)/2);
                        Canvas canvas = getHolder().lockCanvas();
                        if (canvas != null) {
                          //  mCanvas.drawBitmap(mBg,0,0,null);
                            mCanvas.drawPath(mPath, mPaint);
                            mCanvas.drawPath(mClearPath, mEraserPaint);
                            canvas.drawBitmap(mBitmap,0,0,null);
                            getHolder().unlockCanvasAndPost(canvas);
                        }

                    }
                    if (mListener != null){
                        mListener.move(mPreX,mPreY,(x + mPreX)/2,(y + mPreY)/2);
                    }
                }
                mPreX = x;
                mPreY = y;
                break;
            case MotionEvent.ACTION_UP:
                DrawItem drawItem = new DrawItem();
                drawItem.path = mPath;
                drawItem.paint = mPaint;
                mDrawItems.add(drawItem);
                if (mListener != null){
                    mListener.up();
                }
                break;
        }
        return super.onTouchEvent(event);
    }



    private List<DrawItem> mDrawItems = new ArrayList<>();


    public interface MotionEventListener{
        void down(float x,float y);
        void move(float px,float py,float x,float y);
        void up();
        void clear();
    }
    private MotionEventListener mListener;
    public void addListener(MotionEventListener listener){
        mListener = listener;
    }

    private void drawing(){
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawPath(mPath, mPaint);

            canvas.drawPath(mClearPath, mEraserPaint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }



    public void clear(){
        if (mPath != null) {
            mPath.reset();
        }
        if (mListener != null){
            mListener.clear();
        }
    }

    @Override
    public void run() {
        while (isDrawing) {
            drawing();
        }
    }

    public void updateUI(TransInfoBean bean){
        switch (bean.action){
            case DOWN:
                mPath.moveTo(bean.prex,bean.prey);
                break;
            case MOVE:
                mPath.quadTo(bean.prex,bean.prey,bean.curx,bean.cury);
                break;
            case CLEAR:
                mPath.reset();
                break;
        }

    }

    class DrawItem{
        public int pointId = -1;
        public Path path;
        public Paint paint;
    }

}