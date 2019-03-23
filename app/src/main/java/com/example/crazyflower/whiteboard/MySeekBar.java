package com.example.crazyflower.whiteboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MySeekBar extends View implements View.OnTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = "MySeekBar";

    public interface OnSeekBarProgressListener {

        public void onSeekBarProgressChanged(MySeekBar mySeekBar);

    }


    /**
     * 绘制背景条的path和笔
     */
    protected Path background;
    protected Paint backgroundPaint;

    /**
     * 绘制进度条的path和笔
     */
    protected float progress;
    protected Path progressPath;
    protected Paint progressPaint;

    /**
     * 进度条的长度、高度和半高
     */
    protected int seekBarWidth;
    protected int seekBarHeight;
    protected float halfSeekBarHeight;

    /**
     * View的长宽
     */
    protected int width;
    protected int height;
    protected int seekBarLeftX;
    protected int seekBarRightX;
    protected int seekBarTopY;
    protected float seekBarCenterY;

    protected float outerCircleRadius;

    protected Paint outerCirclePaint;
    protected Paint innerCirclePaint;


    /**
     * 手势判别
     */
    private GestureDetector gestureDetector;
    protected MotionEvent lastMotionEvent;
    protected boolean draggable;
    protected boolean scrolling;

    protected OnSeekBarProgressListener onSeekBarProgressListener;


    public MySeekBar(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public MySeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        background = new Path();

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(255, 216, 216, 216));
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

        progress = 1;
        progressPath = new Path();
        progressPaint = new Paint();
        progressPaint.setColor(Color.argb(255, 0, 0, 0));
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setAntiAlias(true);

        outerCirclePaint = new Paint();
        outerCirclePaint.setStyle(Paint.Style.FILL);
        outerCirclePaint.setColor(Color.argb(96, 255, 255, 255));

        innerCirclePaint = new Paint();
        innerCirclePaint.setStyle(Paint.Style.FILL);
        innerCirclePaint.setColor(Color.argb(255, 0, 0, 0));

        gestureDetector = new GestureDetector(this.getContext(), this);
        lastMotionEvent = null;
        draggable = false;
        scrolling = false;

        setOnTouchListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        seekBarLeftX = getPaddingLeft();
        seekBarRightX = w - getPaddingRight();
        seekBarTopY = getPaddingTop();

        seekBarWidth = seekBarRightX - seekBarLeftX;
        seekBarHeight = h - seekBarTopY - getPaddingBottom();
        halfSeekBarHeight = seekBarHeight * 0.5f;
        seekBarCenterY = seekBarTopY + halfSeekBarHeight;

        outerCircleRadius = halfSeekBarHeight + 10;

        background.moveTo(getPaddingLeft(), (h + getPaddingTop() - getPaddingBottom()) / 2f);
        background.lineTo(w - getPaddingRight(), getPaddingTop());
        background.lineTo(w - getPaddingRight(), h - getPaddingBottom());
        background.close();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(background, backgroundPaint);

        progressPath.reset();
        progressPath.moveTo(getPaddingLeft(), getPaddingTop() + seekBarHeight * 0.5f);
        float x = getPaddingLeft() + seekBarWidth * progress * 0.01f;
        float y = getPaddingTop() + seekBarHeight * 0.5f;
        float progressHalfHeight = seekBarHeight * progress * 0.01f * 0.5f;
        progressPath.lineTo(x, y - progressHalfHeight);
        progressPath.lineTo(x, y + progressHalfHeight);
        progressPath.close();
        canvas.drawPath(progressPath, progressPaint);

        canvas.drawCircle(x, y, outerCircleRadius, outerCirclePaint);
        canvas.drawCircle(x, y, progressHalfHeight + 6, innerCirclePaint);
    }


    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        if (progress < 0)
            progress = 0;
        else if (progress > 100)
            progress = 100;
        this.progress = progress;
        invalidate();
        if (null != onSeekBarProgressListener)
            onSeekBarProgressListener.onSeekBarProgressChanged(this);
    }

    public int getColor() {
        return innerCirclePaint.getColor();
    }

    public void setColor(int color) {
        this.progressPaint.setColor(color);
        this.innerCirclePaint.setColor(color);
    }

    public void setOnSeekBarProgressListener(OnSeekBarProgressListener onSeekBarProgressListener) {
        this.onSeekBarProgressListener = onSeekBarProgressListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean result = gestureDetector.onTouchEvent(event);
        if (!result) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (draggable) {
                        onScroll(lastMotionEvent, event, lastMotionEvent.getX() - event.getX(), lastMotionEvent.getY() - event.getY());
                        lastMotionEvent = MotionEvent.obtain(event);
                    }
                    result = true;
                    break;
                case MotionEvent.ACTION_UP:
                    if (draggable) {
                        onScroll(lastMotionEvent, event, lastMotionEvent.getX() - event.getX(), lastMotionEvent.getY() - event.getY());
                        lastMotionEvent = null;
                        draggable = false;
                    } else {
                        afterScroll(event);
                    }
                    result = true;
                    break;
            }
        }

        return result;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "onDown: ");

        draggable = false;
        scrolling = false;
        lastMotionEvent = null;

        float touchX = e.getX();
        float touchY = e.getY();

        float pointX = seekBarLeftX + seekBarWidth * progress * 0.01f;
        float pointY = seekBarCenterY;

        if (Math.pow(pointX - touchX, 2) + Math.pow(pointY - touchY, 2) <= Math.pow(outerCircleRadius, 2)) {
            scrolling = true;
            return true;
        } else
            return seekBarLeftX <= touchX && touchX <= seekBarRightX && Math.abs(pointY - touchY) <= halfSeekBarHeight;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress: ");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp: ");
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll: " + e1.getX() + " " + e1.getY() + " " + e2.getX() + " " + e2.getY());
        if (scrolling || draggable) {
            setProgress(getProgress() - distanceX / seekBarWidth * 100);
            return true;
        }
        return false;
    }

    public void afterScroll(MotionEvent event) {
        Log.d(TAG, "afterScroll: ");
        scrolling = false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling: ");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress: ");
        setProgress((e.getX() - getPaddingLeft()) / seekBarWidth * 100);
        lastMotionEvent = e;
        draggable = true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed: ");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "onDoubleTap: ");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(TAG, "onDoubleTapEvent: ");
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                gestureDetector.setIsLongpressEnabled(false);
                setProgress((e.getX() - getPaddingLeft()) / seekBarWidth * 100);
                break;
            case MotionEvent.ACTION_MOVE:
                setProgress((e.getX() - getPaddingLeft()) / seekBarWidth * 100);
                break;
            case MotionEvent.ACTION_UP:
                gestureDetector.setIsLongpressEnabled(true);
                setProgress((e.getX() - getPaddingLeft()) / seekBarWidth * 100);
                break;
        }
        return true;
    }
}
