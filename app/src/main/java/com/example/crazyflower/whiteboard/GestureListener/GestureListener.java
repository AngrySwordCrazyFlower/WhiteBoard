package com.example.crazyflower.whiteboard.GestureListener;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.DrawView;

import java.lang.ref.WeakReference;

public abstract class GestureListener {

    protected WeakReference<DrawView> drawViewWeakReference;

    public GestureListener(DrawView drawView) {
        drawViewWeakReference = new WeakReference<DrawView>(drawView);
    }

    public abstract void onDraw(Canvas canvas);

    public abstract boolean onTouch(MotionEvent motionEvent);

    public abstract void onCancel();

    protected DrawView getDrawView() {
        return drawViewWeakReference.get();
    }

}
