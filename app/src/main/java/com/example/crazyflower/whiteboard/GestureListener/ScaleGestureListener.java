package com.example.crazyflower.whiteboard.GestureListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.example.crazyflower.whiteboard.DrawView;
import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;
import com.example.crazyflower.whiteboard.Vector2D;

import java.util.HashMap;
import java.util.List;

public class ScaleGestureListener extends GestureListener
//        implements ScaleGestureDetector.OnScaleGestureListener
{


    private static final String TAG = "ScaleGestureListener";

    private ScaleGestureDetector scaleGestureDetector;

    private int state;

    private static final int NO_STATE = 0;
    private static final int SCALING_STATE = 1;
    private static final int TRANSLATING_STATE = 2;

    private PointF centerPointF;

    private int majorPointerId;
    private float majorPointerX;
    private float majorPointerY;
    private int secondaryPointerId;
    private float secondaryPointerX;
    private float secondaryPointerY;

    private SparseArray<PointF> pointerRecord;

    public ScaleGestureListener(Context context) {
        super(context);
        state = NO_STATE;
//        scaleGestureDetector = new ScaleGestureDetector(context, this);
        centerPointF = new PointF();
        pointerRecord = new SparseArray<PointF>();

        majorPointerId = -1;
        secondaryPointerId = -1;
    }

    @Override
    public void onDraw(Canvas canvas) {

    }

    @Override
    public ActionWrapper onTouch(List<BasicElement> elements, MotionEvent motionEvent, Matrix matrix) {
        ActionWrapper result = new ActionWrapper();
//        String info = "";
//        for (int i = 0; i <motionEvent.getPointerCount(); i++) {
//            info = info + "index: " + i + ", id: " + motionEvent.getPointerId(i) + ", x: " + motionEvent.getX() + ", y: " + motionEvent.getY(i) + "\n";
//        }
//        Log.d(TAG, "onTouch: " + info);
        int id, actionIndex, count;
        float totalX, totalY;
        float tempX, tempY, scale;
        Vector2D a, b;
        PointF tempPointF;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 实际上肯定是0
//                actionIndex = motionEvent.getActionIndex();
//                majorPointerId = motionEvent.getPointerId(actionIndex);
//                majorPointerX = motionEvent.getX(actionIndex);
//                majorPointerY = motionEvent.getY(actionIndex);
//                break;
            case MotionEvent.ACTION_POINTER_DOWN:
//                if (secondaryPointerId == -1) {
//                    actionIndex = motionEvent.getActionIndex();
//                    secondaryPointerId = motionEvent.getPointerId(actionIndex);
//                    secondaryPointerX = motionEvent.getX(actionIndex);
//                    secondaryPointerY = motionEvent.getY(actionIndex);
//                }
                actionIndex = motionEvent.getActionIndex();
                pointerRecord.put(motionEvent.getPointerId(actionIndex), new PointF(motionEvent.getX(actionIndex), motionEvent.getY(actionIndex)));
//                Log.d(TAG, "onTouch: pointer_down" + motionEvent.getActionIndex());
                break;
            case MotionEvent.ACTION_MOVE:
                count = motionEvent.getPointerCount();
                if (count > 1) {
                    totalX = 0;
                    totalY = 0;
                    for (int i = 0; i < count; i++) {
                        id = motionEvent.getPointerId(i);

                        tempPointF = pointerRecord.get(id);

                        totalX += tempPointF.x;
                        totalY += tempPointF.y;
                    }

                    totalX /= count;
                    totalY /= count;
                    scale = 1;
                    for (int i = 0; i < count; i++) {
                        id = motionEvent.getPointerId(i);

                        tempPointF = pointerRecord.get(id);

                        a = new Vector2D(tempPointF.x - totalX, tempPointF.y - totalY);
                        b = new Vector2D(motionEvent.getX(i) - totalX, motionEvent.getY(i) - totalY);
                        tempPointF.x = motionEvent.getX(i);
                        tempPointF.y = motionEvent.getY(i);
                        scale *= a.dot(b) / (float) Math.pow(a.length(), 2);
                    }

//                    String info = "";
//                    for (int i = 0; i < motionEvent.getPointerCount(); i++) {
//                        info = info + "index: " + i + ", id: " + motionEvent.getPointerId(i) + ", x: " + motionEvent.getX(i) + ", y: " + motionEvent.getY(i) + "\n";
//                    }
//                    Log.d(TAG, "onTouch info: " + info);


//                    actionIndex = motionEvent.getActionIndex();
//                    id = motionEvent.getPointerId(actionIndex);
//                    tempPointF = pointerRecord.get(id);
//                    tempX = motionEvent.getX(id);
//                    tempY = motionEvent.getY(id);
//
//
//                    Log.d(TAG, "onTouch action index: " + actionIndex + ", id: " + id + ", x: " + tempX + ", y: " + tempY);
//
////                    Log.d(TAG, "onTouch: " + tempX + " " + tempY);
//                    a = new Vector2D(tempPointF.x - totalX, tempPointF.y - totalY);
//                    b = new Vector2D(tempX - totalX, tempY - totalY);
//                    tempPointF.x = tempX;
//                    tempPointF.y = tempY;
                    Log.d(TAG, "onTouch: totalX:" + totalX + ", totalY: " + totalY);
                    tempPointF = new PointF(totalX, totalY);
//                    Log.d(TAG, "onTouch: " + tempPointF.x + " " + tempPointF.y);
                    DrawViewUtil.transformPoint(tempPointF, matrix);
                    Log.d(TAG, "onTouch: scaleX:" + tempPointF.x + ", scaleY: " + tempPointF.y);
//                    scale = a.dot(b) / (float) Math.pow(a.length(), 2);
//                    Log.d(TAG, "onTouch: " + scale + " " + tempPointF.x + " " + tempPointF.y);
                    result.setCanvasScale(scale, tempPointF.x, tempPointF.y);
                } else {
                    id = motionEvent.getPointerId(motionEvent.getActionIndex());
                    tempPointF = pointerRecord.get(id);
                    tempX = motionEvent.getX();
                    tempY = motionEvent.getY();
                    Log.d(TAG, "onTouch: oldX: " + tempPointF.x + ", oldY: " + tempPointF.y + ", newX: " + tempX + ", newY: " + tempY);
                    result.setCanvasTranslateDx(tempX - tempPointF.x);
                    result.setCanvasTranslateDy(tempY - tempPointF.y);
                    tempPointF.x = tempX;
                    tempPointF.y = tempY;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
//                Log.d(TAG, "onTouch: " + motionEvent.getActionIndex());

                pointerRecord.remove(motionEvent.getPointerId(motionEvent.getActionIndex()));

                break;
        }

        return result;

//        scaleGestureDetector.onTouchEvent(motionEvent);
//
//        PointF tempPointF;
//        switch (motionEvent.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_POINTER_DOWN:
//                for (int i = 0, length = motionEvent.getPointerCount(); i < length; i++) {
//                    pointerRecord.put(motionEvent.getPointerId(i), new PointF(motionEvent.getX(i), motionEvent.getY(i)));
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (NO_STATE == state || TRANSLATING_STATE == state) {
//                    state = TRANSLATING_STATE;
//                    float dx = 0, dy = 0;
//                    for (int i = 0, length = motionEvent.getPointerCount(); i < length; i++) {
//                        if (null != (tempPointF = pointerRecord.get(motionEvent.getPointerId(i)))) {
//                            dx += motionEvent.getX(i) - tempPointF.x;
//                            dy += motionEvent.getY(i) - tempPointF.y;
//                        }
//                    }
//                    drawView.onCanvasTranslate(drawView.transformEventDistanceToCanvas(dx), drawView.transformEventDistanceToCanvas(dy));
//                    drawView.invalidate();
//                }
//                for (int i = 0, length = motionEvent.getPointerCount(); i < length; i++) {
//                    pointerRecord.put(motionEvent.getPointerId(i), new PointF(motionEvent.getX(i), motionEvent.getY(i)));
//                }
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//            case MotionEvent.ACTION_UP:
//                if (TRANSLATING_STATE == state)
//                    state = NO_STATE;
//                pointerRecord.remove(motionEvent.getPointerId(motionEvent.getActionIndex()));
//                break;
//        }
//        return true;
    }

    @Override
    public ActionWrapper onCancel() {
        return null;
//        state = NO_STATE;
//        pointerRecord.clear();
    }

//    @Override
//    public boolean onScale(ScaleGestureDetector detector) {
//        DrawView drawView = getDrawView();
//        if (null == drawView)
//            return false;
//        if (SCALING_STATE == state) {
//            centerPointF.x = detector.getFocusX();
//            centerPointF.y = detector.getFocusY();
//            drawView.transformEventCoordinateToCanvas(centerPointF);
//            drawView.onCanvasScale(detector.getScaleFactor(), centerPointF.x, centerPointF.y);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onScaleBegin(ScaleGestureDetector detector) {
//        state = SCALING_STATE;
//        return true;
//    }
//
//    @Override
//    public void onScaleEnd(ScaleGestureDetector detector) {
//        state = NO_STATE;
//    }

}
