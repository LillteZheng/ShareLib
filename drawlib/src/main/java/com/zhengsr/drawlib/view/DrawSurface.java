package com.zhengsr.drawlib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe:
 */
public class DrawSurface extends TextureView {
    public DrawSurface(Context context) {
        this(context,null);
    }

    public DrawSurface(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DrawSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOpaque(true);
        setClickable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }
}
