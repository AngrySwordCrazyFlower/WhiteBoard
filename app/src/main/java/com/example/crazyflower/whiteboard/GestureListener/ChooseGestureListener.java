package com.example.crazyflower.whiteboard.GestureListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.ArrayList;
import java.util.List;

public class ChooseGestureListener extends GestureListener {

    public static final String TAG = "ChooseGestureListener";

    private int state;

    private static final int IDLE_STATE = 0;
    private static final int DOWN_STATE = 1;
    private static final int MOVE_STATE = 2;

    private float left, top, right, bottom;

    private int downPointerId;

    private GestureListener chosenRegionGestureListener;

    private Paint choosePaint;

    private RectF rectF;

    private PointF pointF;

    public ChooseGestureListener(Context context, Paint choosePaint) {
        super(context);
        state = IDLE_STATE;
        chosenRegionGestureListener = null;
        this.choosePaint = choosePaint;
        rectF = new RectF();
        pointF = new PointF();
    }

    @Override
    public ActionWrapper onTouch(List<BasicElement> elements, MotionEvent motionEvent, Matrix matrix) {
        // 优先交给下层（选中区域的手势处理者）处理
        if (chosenRegionGestureListener != null) {
            ActionWrapper subordinateResult = chosenRegionGestureListener.onTouch(elements, motionEvent, matrix);
            if (subordinateResult != null) {
                if (!subordinateResult.isHoldOn()) {
                    chosenRegionGestureListener.onCancel();
                    chosenRegionGestureListener = null;
                }
                return subordinateResult;
            }
        }
        // 到这一步，说明下层（选中区域的手势处理者）不处理，这个手势触摸由选择区域来处理
        ActionWrapper result = new ActionWrapper();
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                pointF.x = motionEvent.getX();
                pointF.y = motionEvent.getY();
                DrawViewUtil.transformPoint(pointF, matrix);
                state = DOWN_STATE;
                left = right = pointF.x;
                top = bottom = pointF.y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (IDLE_STATE != state) {
//                        onScroll(result, motionEvent);
                        state = MOVE_STATE;

                        pointF.x = motionEvent.getX();
                        pointF.y = motionEvent.getY();
                        DrawViewUtil.transformPoint(pointF, matrix);

                        right = pointF.x;
                        bottom = pointF.y;

                        DrawViewUtil.makeRectF(rectF, left, top, right, bottom);
                        result.setNeedInvalidate(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
//                    onFingerUp(result, motionEvent, elements);
                    // 先取消旧的
                    if (chosenRegionGestureListener != null) {
                        chosenRegionGestureListener.onCancel();
                        result.setNeedInvalidate(true);
                        chosenRegionGestureListener = null;
                    }
                    // 在选中区域外单击, 这时计算的话应该也是空，但是我怕会出现特殊情况，直接判了
                    if (state == DOWN_STATE)
                        state = IDLE_STATE;
                    else {

                        // 到了这里, state肯定是MOVE_STATE,那么肯定要刷新（为了消除显示的虚线）
                        result.setNeedInvalidate(true);

                        pointF.x = motionEvent.getX();
                        pointF.y = motionEvent.getY();
                        DrawViewUtil.transformPoint(pointF, matrix);
                        right = pointF.x;
                        bottom = pointF.y;
                        DrawViewUtil.makeRectF(rectF, left, top, right, bottom);
                        // 计算哪些被选中了
                        List<BasicElement> chosenElements = new ArrayList<>();
                        for (BasicElement basicElement : elements)
                            if (basicElement.isCovered(rectF))
                                chosenElements.add(basicElement);
                        // 非空，则应该有新的处理者
                        if (!chosenElements.isEmpty())
                            chosenRegionGestureListener = new ChosenRegionGestureListener(getContext(), chosenElements);

                        state = IDLE_STATE;
                    }
                }
                break;
        }
        return result;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (null != chosenRegionGestureListener)
            chosenRegionGestureListener.onDraw(canvas);
        if (MOVE_STATE == state) {
            canvas.drawRect(rectF, choosePaint);
        }
    }

    private void onDown(ActionWrapper actionWrapper, MotionEvent motionEvent) {
        state = DOWN_STATE;
        left = right = motionEvent.getX();
        top = bottom = motionEvent.getY();
    }

    private void onScroll(ActionWrapper actionWrapper, MotionEvent motionEvent) {
        state = MOVE_STATE;
        right = motionEvent.getX();
        bottom = motionEvent.getY();
        DrawViewUtil.makeRectF(rectF, left, top, right, bottom);
        actionWrapper.setNeedInvalidate(true);
    }

    private void onFingerUp(ActionWrapper actionWrapper, MotionEvent motionEvent, List<BasicElement> elements) {
        // 先取消旧的
        if (chosenRegionGestureListener != null) {
            chosenRegionGestureListener.onCancel();
            actionWrapper.setNeedInvalidate(true);
            chosenRegionGestureListener = null;
        }
        // 在选中区域外单击, 这时计算的话应该也是空，但是我怕会出现特殊情况，直接判了
        if (state == DOWN_STATE) {
            state = IDLE_STATE;
            return;
        }

        // 到了这里, state肯定是MOVE_STATE,那么肯定要刷新（为了消除显示的虚线）
        actionWrapper.setNeedInvalidate(true);

        right = motionEvent.getX();
        bottom = motionEvent.getY();
        DrawViewUtil.makeRectF(rectF, left, top, right, bottom);
        // 计算哪些被选中了
        List<BasicElement> chosenElements = new ArrayList<>();
        for (BasicElement basicElement : elements)
            if (basicElement.isCovered(rectF))
                chosenElements.add(basicElement);
        // 非空，则应该有新的处理者
        if (!chosenElements.isEmpty())
            chosenRegionGestureListener = new ChosenRegionGestureListener(getContext(), chosenElements);

        state = IDLE_STATE;
    }

    @Override
    public ActionWrapper onCancel() {
        state = IDLE_STATE;
        ActionWrapper result = null;
        if (null != chosenRegionGestureListener) {
            result = chosenRegionGestureListener.onCancel();
            result.setNeedInvalidate(true);
            chosenRegionGestureListener = null;
        }
        return result;
    }

}
