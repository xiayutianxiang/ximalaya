package com.example.ximalaya.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.ximalaya.R;

@SuppressLint("AppCompatCustomView")
public class LoadingView extends ImageView {

    //旋转的角度
    private int rotateDegree = 0;
    private boolean mNeedRotate = false;
    public LoadingView(Context context) {
        this(context,null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //设置图片
        setImageResource(R.mipmap.loading);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mNeedRotate = true;
        //绑定到window时
        post(new Runnable() {
            @Override
            public void run() {
                rotateDegree += 30;
                rotateDegree = rotateDegree <= 360 ? rotateDegree:0;
                invalidate();
                //是否继续旋转
                postDelayed(this,100);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //从window解绑
        mNeedRotate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * 绘制时复写ondraw
         * 第一个参数是旋转角度
         * 第二个参数是旋转的x坐标
         * 第三个参数是旋转的y坐标
         */
        canvas.rotate(rotateDegree,getWidth()/2,getHeight()/2);
        super.onDraw(canvas);
    }
}
