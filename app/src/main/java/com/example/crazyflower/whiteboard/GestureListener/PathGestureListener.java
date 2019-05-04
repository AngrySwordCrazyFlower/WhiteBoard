package com.example.crazyflower.whiteboard.GestureListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.Action.Action;
import com.example.crazyflower.whiteboard.Action.NewAction;
import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;
import com.example.crazyflower.whiteboard.Element.PathElement;
import com.example.crazyflower.whiteboard.ParcelablePath;

import java.util.List;

public class PathGestureListener extends GestureListener {

    private static final String TAG = "PathGestureListener";

    private ParcelablePath parcelablePath;

    private int activePointerId;

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
        int index;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                parcelablePath.reset();

                pointF.x = motionEvent.getX();
                pointF.y = motionEvent.getY();
                DrawViewUtil.transformPoint(pointF, matrix);

                parcelablePath.moveTo(pointF.x, pointF.y);
                state = DOWN_STATE;
                activePointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                break;
            case MotionEvent.ACTION_MOVE:
                // 根据当前那根手指的id，更新坐标
                index = motionEvent.findPointerIndex(activePointerId);
                pointF.x = motionEvent.getX(index);
                pointF.y = motionEvent.getY(index);
                DrawViewUtil.transformPoint(pointF, matrix);
                // 这里可能会重复x,y，记得修复
                // 重复x,y指当前手指没动，其他手指的运动导致line to同一个点多次，这样是浪费的。
                state = MOVE_STATE;
                parcelablePath.lineTo(pointF.x, pointF.y);
                result.setNeedInvalidate(true);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                index = motionEvent.getActionIndex();
                if (motionEvent.findPointerIndex(index) == activePointerId) {
                    if (index == 0) {
                        activePointerId = motionEvent.getPointerId(1);
                        pointF.x = motionEvent.getX(1);
                        pointF.y = motionEvent.getY(1);
                        DrawViewUtil.transformPoint(pointF, matrix);
                        parcelablePath.lineTo(pointF.x, pointF.y);
                    }
                    else {
                        activePointerId = motionEvent.getPointerId(0);
                        pointF.x = motionEvent.getX(0);
                        pointF.y = motionEvent.getY(0);
                        DrawViewUtil.transformPoint(pointF, matrix);
                        parcelablePath.lineTo(pointF.x, pointF.y);
                    }
                    result.setNeedInvalidate(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (MOVE_STATE == state) {
                    result.setAction(generateAction());
                    result.setNeedInvalidate(true);
                }
                state = IDLE_STATE;
                break;
        }
        return result;
    }

    @Override
    public ActionWrapper onCancel() {
        ActionWrapper result = new ActionWrapper();
        if (state == MOVE_STATE) {
            result.setAction(generateAction());
            result.setNeedInvalidate(true);
        }
        state = IDLE_STATE;
        return result;
    }

    private Action generateAction() {
        return new NewAction(new PathElement(new ParcelablePath(parcelablePath), new Paint(paint)));
    }

}
