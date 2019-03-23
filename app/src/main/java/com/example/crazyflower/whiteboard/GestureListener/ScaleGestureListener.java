package com.example.crazyflower.whiteboard.GestureListener;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.example.crazyflower.whiteboard.DrawView;

import java.util.HashMap;

public class ScaleGestureListener extends GestureListener implements ScaleGestureDetector.OnScaleGestureListener {


    private static final String TAG = "ScaleGestureListener";

    private ScaleGestureDetector scaleGestureDetector;

    private int state;

    private static final int NO_STATE = 0;
    private static final int SCALING_STATE = 1;
    private static final int TRANSLATING_STATE = 2;

    private PointF centerPointF;

    private HashMap<Integer, PointF> pointerRecord;

    public ScaleGestureListener(DrawView drawView) {
        super(drawView);
        state = NO_STATE;
        scaleGestureDetector = new ScaleGestureDetector(drawView.getContext(), this);
        centerPointF = new PointF();
        pointerRecord = new HashMap<Integer, PointF>();
    }

    @Override
    public void onDraw(Canvas canvas) {

    }

    @Override
    public boolean onTouch(MotionEvent motionEvent) {
        DrawView drawView = getDrawView();
        if (null == drawView)
            return false;
        scaleGestureDetector.onTouchEvent(motionEvent);

        PointF tempPointF;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                for (int i = 0, length = motionEvent.getPointerCount(); i < length; i++) {
                    pointerRecord.put(motionEvent.getPointerId(i), new PointF(motionEvent.getX(i), motionEvent.getY(i)));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (NO_STATE == state || TRANSLATING_STATE == state) {
                    state = TRANSLATING_STATE;
                    float dx = 0, dy = 0;
                    for (int i = 0, length = motionEvent.getPointerCount(); i < length; i++) {
                        if (null != (tempPointF = pointerRecord.get(motionEvent.getPointerId(i)))) {
                            dx += motionEvent.getX(i) - tempPointF.x;
                            dy += motionEvent.getY(i) - tempPointF.y;
                        }
                    }
                    drawView.onCanvasTranslate(drawView.transformEventDistanceToCanvas(dx), drawView.transformEventDistanceToCanvas(dy));
                    drawView.invalidate();
                }
                for (int i = 0, length = motionEvent.getPointerCount(); i < length; i++) {
                    pointerRecord.put(motionEvent.getPointerId(i), new PointF(motionEvent.getX(i), motionEvent.getY(i)));
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (TRANSLATING_STATE == state)
                    state = NO_STATE;
                pointerRecord.remove(motionEvent.getPointerId(motionEvent.getActionIndex()));
                break;
        }
        return true;
    }

    @Override
    public void onCancel() {
        state = NO_STATE;
        pointerRecord.clear();
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        DrawView drawView = getDrawView();
        if (null == drawView)
            return false;
        if (SCALING_STATE == state) {
            centerPointF.x = detector.getFocusX();
            centerPointF.y = detector.getFocusY();
            drawView.transformEventCoordinateToCanvas(centerPointF);
            drawView.onCanvasScale(detector.getScaleFactor(), centerPointF.x, centerPointF.y);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        state = SCALING_STATE;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        state = NO_STATE;
    }

}
