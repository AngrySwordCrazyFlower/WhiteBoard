package com.example.crazyflower.whiteboard.GestureListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.DrawView;
import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class GestureListener {

//    protected WeakReference<DrawView> drawViewWeakReference;

//    public GestureListener(DrawView drawView) {
//        drawViewWeakReference = new WeakReference<DrawView>(drawView);
//    }

    protected Context context;

    public GestureListener(Context context) {
        this.context = context;
    }

    public abstract void onDraw(Canvas canvas);

    /**
     * 考虑责任链模式，该函数是主要的分发任务函数，返回null说明这个任务我不接
     * 返回不是null的话，这个任务我接了，而且肯定有个结果，至于结果怎么样，leader自己决定。
     */
    public abstract ActionWrapper onTouch(List<BasicElement> elements, MotionEvent motionEvent, Matrix matrix);

    public abstract ActionWrapper onCancel();

    public Context getContext() {
        return context;
    }
}
