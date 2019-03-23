package com.example.crazyflower.whiteboard.GestureListener;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.DrawView;
import com.example.crazyflower.whiteboard.DrawViewUtil;

public class RectFGestureListener extends GestureListener {

    private static final String TAG = "RegionGestureListener";

    private int state;

    private static final int NO_STATE = 0;
    private static final int DOWN_STATE = 1;
    private static final int MOVE_STATE = 2;

    private PointF downPointF;
    private PointF scrollPointF;

    private RectF rectF;

    private int downPointerId;

    public RectFGestureListener(DrawView drawView) {
        super(drawView);
        rectF = new RectF();
        downPointF = new PointF();
        scrollPointF = new PointF();
        state = NO_STATE;
    }

    @Override
    public void onDraw(Canvas canvas) {
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return;
        if (MOVE_STATE == state) {
            canvas.drawRect(rectF, drawView.getRegionPaint());
        }
    }

    @Override
    public boolean onTouch(MotionEvent motionEvent) {
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return false;
//        Log.d(TAG, "onTouch: " + motionEvent.getAction());
        boolean result = false;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (NO_STATE == state) {
                    state = DOWN_STATE;
                    downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    drawView.transformEventCoordinateToCanvas(motionEvent, downPointF);
                    onDown();
                    result = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (NO_STATE != state) {
                        state = MOVE_STATE;
                        drawView.transformEventCoordinateToCanvas(motionEvent, scrollPointF);
                        onScroll();
                        result = true;
                        drawView.invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (DOWN_STATE == state) {
                        onClick();
                        state = NO_STATE;
                        result = true;
                    } else if (MOVE_STATE == state) {
                        drawView.onRectFEnd(rectF);
                        state = NO_STATE;
                        result = true;
                    }
                }
                break;
        }
        return result;
    }

    private void onDown() {
        rectF.left = downPointF.x;
        rectF.top = downPointF.y;
        rectF.right = downPointF.x;
        rectF.bottom = downPointF.y;
    }

    public void onScroll() {
        Log.d(TAG, "onScroll: ");
        DrawViewUtil.makeRectF(rectF, downPointF.x, downPointF.y, scrollPointF.x, scrollPointF.y);
    }

    private void onClick() {

    }

    @Override
    public void onCancel() {
        state = NO_STATE;
    }
}
