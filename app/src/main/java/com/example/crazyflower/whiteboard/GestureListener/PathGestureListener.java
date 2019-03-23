package com.example.crazyflower.whiteboard.GestureListener;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.DrawView;

public class PathGestureListener extends GestureListener {

    private static final String TAG = "PathGestureListener";

    private Path path;

    private int downPointerId;

    private PointF pointF;

    private int state;

    private static final int NO_STATE = 0;
    private static final int DOWN_STATE = 1;
    private static final int MOVE_STATE = 2;

    public PathGestureListener(DrawView drawView) {
        super(drawView);
        path = new Path();
        pointF = new PointF();
        state = NO_STATE;
    }

    @Override
    public void onDraw(Canvas canvas) {
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return;
        if (MOVE_STATE == state) {
            canvas.drawPath(path, drawView.getPathPaint());
        }
    }

    public boolean onTouch(MotionEvent motionEvent) {
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return false;
//        Log.d(TAG, "onTouch: " + motionEvent.getActionMasked());
        boolean result = false;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (NO_STATE == state) {
                    drawView.transformEventCoordinateToCanvas(motionEvent, pointF);
                    path.reset();
                    path.moveTo(pointF.x, pointF.y);
                    state = DOWN_STATE;
                    downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    result = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (NO_STATE != state) {
                        state = MOVE_STATE;
                        drawView.transformEventCoordinateToCanvas(motionEvent, pointF);
                        onScroll();
                        result = true;
                        drawView.invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (MOVE_STATE == state) {
                        state = NO_STATE;
                        drawView.onPathEnd(path);
                        result = true;
                    } else if (DOWN_STATE == state) {
                        state = NO_STATE;
                        result = true;
                    }
                }
                break;
        }
        return result;
    }

    private void onScroll() {
        Log.d(TAG, "onScroll: ");
        path.lineTo(pointF.x, pointF.y);
    }

    @Override
    public void onCancel() {
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return;
        if (NO_STATE != state) {
            drawView.onPathEnd(path);
            state = NO_STATE;
        }
    }

}
