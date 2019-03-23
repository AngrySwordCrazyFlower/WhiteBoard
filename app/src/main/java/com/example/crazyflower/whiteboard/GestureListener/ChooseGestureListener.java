package com.example.crazyflower.whiteboard.GestureListener;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.DrawView;
import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.ArrayList;
import java.util.List;

public class ChooseGestureListener extends GestureListener {

    public static final String TAG = "ChooseGestureListener";

    private int state;

    private static final int NO_STATE = 0;
    private static final int DOWN_STATE = 1;
    private static final int MOVE_STATE = 2;

    private PointF downPointF;
    private PointF scrollPointF;

    private RectF rectF;

    private int downPointerId;

    private ChosenRegionGestureListener chosenRegionGestureListener;

    public ChooseGestureListener(DrawView drawView) {
        super(drawView);
        state = NO_STATE;
        downPointF = new PointF();
        scrollPointF = new PointF();
        rectF = new RectF();
        chosenRegionGestureListener = null;
    }

    @Override
    public boolean onTouch(MotionEvent motionEvent) {
        DrawView drawView = getDrawView();
        if (null == drawView)
            return false;
        boolean result = false;
        if (null != chosenRegionGestureListener)
            result = chosenRegionGestureListener.onTouch(motionEvent);
        if (!result) {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    drawView.transformEventCoordinateToCanvas(motionEvent, downPointF);
                    downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    state = DOWN_STATE;
                    result = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                        if (NO_STATE != state) {
                            drawView.transformEventCoordinateToCanvas(motionEvent, scrollPointF);
                            state = MOVE_STATE;
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
                            drawView.invalidate();
                        } else if (MOVE_STATE == state) {
                            onScrollEnd();
                            state = NO_STATE;
                            result = true;
                            drawView.invalidate();
                        }
                    }
                    break;
            }
        }
        return result;
    }

    @Override
    public void onDraw(Canvas canvas) {
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return;
        if (null != chosenRegionGestureListener)
            chosenRegionGestureListener.onDraw(canvas);
        if (MOVE_STATE == state)
            canvas.drawRect(rectF, drawView.getChoosePaint());
    }

    private void onScroll() {
        DrawViewUtil.makeRectF(rectF, downPointF.x, downPointF.y, scrollPointF.x, scrollPointF.y);
    }

    private void onScrollEnd() {
        onCancel();
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return;

        List<BasicElement> chosenElements = new ArrayList<>();

        for (BasicElement basicElement : drawView.getElements())
            if (basicElement.isCovered(rectF))
                chosenElements.add(basicElement);
        if (!chosenElements.isEmpty())
            chosenRegionGestureListener = new ChosenRegionGestureListener(drawView, this, chosenElements);
    }

    private void onClick() {
        if (null != chosenRegionGestureListener) {
            chosenRegionGestureListener.onCancel();
            chosenRegionGestureListener = null;
        }
    }

    @Override
    public void onCancel() {
        if (null != chosenRegionGestureListener) {
            chosenRegionGestureListener.onCancel();
            chosenRegionGestureListener = null;
        }
        state = NO_STATE;
    }

    public void onChosenElementsDelete() {
        DrawView drawView = drawViewWeakReference.get();
        if (null == drawView)
            return;
        drawView.onChooseElementsDelete(chosenRegionGestureListener.getChosenElements());
        chosenRegionGestureListener = null;
        state = NO_STATE;
    }

}
