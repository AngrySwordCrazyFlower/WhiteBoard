package com.example.crazyflower.whiteboard.GestureListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.Action.NewAction;
import com.example.crazyflower.whiteboard.DrawView;
import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;
import com.example.crazyflower.whiteboard.Element.PathElement;
import com.example.crazyflower.whiteboard.ParcelablePath;

import java.util.List;

public class PathGestureListener extends GestureListener {

    private static final String TAG = "PathGestureListener";

    private ParcelablePath parcelablePath;

    private int downPointerId;

    private PointF pointF;

    private int state;

    private Paint paint;

    private static final int IDLE_STATE = 0;
    private static final int DOWN_STATE = 1;
    private static final int MOVE_STATE = 2;

    public PathGestureListener(Context context, Paint paint) {
        super(context);
        parcelablePath = new ParcelablePath();
        pointF = new PointF();
        state = IDLE_STATE;
        this.paint = paint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (MOVE_STATE == state) {
            canvas.drawPath(parcelablePath.getPath(), paint);
        }
    }

    @Override
    public ActionWrapper onTouch(List<BasicElement> elements, MotionEvent motionEvent, Matrix matrix) {
//        Log.d(TAG, "onTouch: " + motionEvent.getActionMasked());
        ActionWrapper result = new ActionWrapper();

        pointF.x = motionEvent.getX();
        pointF.y = motionEvent.getY();
        DrawViewUtil.transformPoint(pointF, matrix);

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (IDLE_STATE == state) {
                    parcelablePath.reset();
                    parcelablePath.moveTo(pointF.x, pointF.y);
                    state = DOWN_STATE;
                    downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    onScroll(result);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (MOVE_STATE == state)
                        onPathEnd(result);
                    else if (DOWN_STATE == state)
                        state = IDLE_STATE;
                }
                break;
        }
        return result;
    }

    private void onScroll(ActionWrapper actionWrapper) {
        Log.d(TAG, "onScroll: " + pointF.x + " " + pointF.y);
        state = MOVE_STATE;
        parcelablePath.lineTo(pointF.x, pointF.y);
        actionWrapper.setNeedInvalidate(true);
    }

    @Override
    public ActionWrapper onCancel() {
        ActionWrapper result = new ActionWrapper();
        if (state == MOVE_STATE)
            onPathEnd(result);
        return result;
    }

    private void onPathEnd(ActionWrapper actionWrapper) {
        actionWrapper.setAction(new NewAction(new PathElement(new ParcelablePath(parcelablePath), new Paint(paint))));
        actionWrapper.setNeedInvalidate(true);
        state = IDLE_STATE;
    }

}
