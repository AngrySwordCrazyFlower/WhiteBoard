package com.example.crazyflower.whiteboard;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MyRecyclerView extends RecyclerView {

    private static final String TAG = "MyRecyclerView";

    public MyRecyclerView(Context context) {
        super(context);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d(TAG, "onTouchEvent: start " + e.getAction());
        boolean result = super.onTouchEvent(e);
        Log.d(TAG, "onTouchEvent: end " + result);
        return result;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "dispatchTouchEvent: start " + ev.getAction());
        boolean result = super.dispatchTouchEvent(ev);
        Log.d(TAG, "dispatchTouchEvent: end " + result);
        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        Log.d(TAG, "onInterceptTouchEvent: start " + e.getAction());
        boolean result = super.onInterceptTouchEvent(e);
        Log.d(TAG, "onInterceptTouchEvent: end " + result);
        return result;
    }
}
