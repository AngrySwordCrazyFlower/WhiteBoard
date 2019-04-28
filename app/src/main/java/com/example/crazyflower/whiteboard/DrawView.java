package com.example.crazyflower.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.crazyflower.whiteboard.Action.ActionHistoryManager;

import com.example.crazyflower.whiteboard.Action.NewAction;
import com.example.crazyflower.whiteboard.Element.BasicElement;

import com.example.crazyflower.whiteboard.Element.BitmapElement;
import com.example.crazyflower.whiteboard.GestureListener.ActionWrapper;
import com.example.crazyflower.whiteboard.GestureListener.ChooseGestureListener;
import com.example.crazyflower.whiteboard.GestureListener.GestureListener;
import com.example.crazyflower.whiteboard.GestureListener.PathGestureListener;
import com.example.crazyflower.whiteboard.GestureListener.ScaleGestureListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrawView extends View implements View.OnTouchListener {

    public interface ScaleChangeListener {

        /**
         *
         * Called when the scale of DrawView changed.
         *
         * @param scale The new scale factor.
         * @param oldScale The old scale factor.
         */
        void onScaleChanged(float scale, float oldScale);

    }

    public static final int PAINT_MODE = 0x100001;

    public static final int CHOOSE_MODE = 0x100002;

    public static final int SCALE_TRANSLATING_MODE = 0x100004;

    private static final String TAG = "DrawView";

    /**
     * 当前的选中模式
     */
    private int currentMode;

    /**
     * 笔的画笔
     */
    private Paint pathPaint;

    /**
     * 绘制选择区域的画笔
     */
    private Paint choosePaint;

    /**
     * 管理历史记录的
     */
    private ActionHistoryManager actionHistoryManager;

    /**
     * 当前模式的手势处理者
     */
    private GestureListener gestureListener;

    /**
     * 画布缩放倍数的监听者
     */
    protected Set<ScaleChangeListener> scaleChangeListenerSet;

    protected Matrix canvasTransformMatrix;

    protected float[] transformMatrixValues;

    protected float[] dashPathEffect;

    public static final float DEFAULT_DASH_PATH_EFFECT = 5f;
    public static final float DEFAULT_CHOOSE_PAINT_STROKE_WIDTH = 6f;

    public DrawView(Context context) {
        this(context, null);
        Log.d(TAG, "DrawView: " + getWidth() + " " + getHeight());
    }

    public DrawView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

//        elements = new ArrayList<BasicElement>();

        pathPaint = new Paint();
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(DrawViewUtil.PAINT_MIN_STROKE_WIDTH);
        pathPaint.setColor(Color.rgb(0, 0, 0));

        choosePaint = new Paint();
        choosePaint.setStyle(Paint.Style.STROKE);
        choosePaint.setColor(Color.rgb(0, 0, 0));
        dashPathEffect = new float[] {DEFAULT_DASH_PATH_EFFECT, DEFAULT_DASH_PATH_EFFECT};
        choosePaint.setStrokeWidth(DEFAULT_CHOOSE_PAINT_STROKE_WIDTH);
        choosePaint.setPathEffect(new DashPathEffect(dashPathEffect, 0));

        actionHistoryManager = new ActionHistoryManager();

        // 对该View关闭硬件加速，不关闭的话有些地方可能绘制的结果不一样
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        setOnTouchListener(this);

        canvasTransformMatrix = new Matrix();
        transformMatrixValues = new float[9];

        scaleChangeListenerSet = new HashSet<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*
         * 初始的时候，画布的原点位于View的中心点，所以是w / 2, h / 2
         * 在后来大小改变的时候，offsetX、offsetY已经改变了，应该减去上次的偏移量，加上这次的偏移量，以使得用户进行的拖动没有被影响
         */
        scroll((w - oldw) / 2f, (h - oldh) / 2f, true);
    }

    private void drawHistory(Canvas canvas) {
        List<BasicElement> elements = actionHistoryManager.getOnCanvasBasicElements();
        Log.d(TAG, "drawHistory: " + elements.size());
        for (BasicElement object : elements) {
            object.draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw: ");

        canvasTransformMatrix.getValues(transformMatrixValues);
        Log.d(TAG, "onDraw: " + transformMatrixValues[0] + " " + transformMatrixValues[2] + " " + transformMatrixValues[5]);
        // 根据当前画布的偏移、缩放进行调整
        canvas.translate(transformMatrixValues[2], transformMatrixValues[5]);
        canvas.scale(transformMatrixValues[0], transformMatrixValues[0]);

        // 绘制已经画了的一些元素
        drawHistory(canvas);

        if (null != gestureListener)
            gestureListener.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (null != gestureListener) {
            ActionWrapper actionWrapper = gestureListener.onTouch(actionHistoryManager.getOnCanvasBasicElements(), event, canvasTransformMatrix);
            if (actionWrapper != null) {
                if (actionWrapper.getAction() != null)
                    actionHistoryManager.addAction(actionWrapper.getAction());
                Log.d(TAG, "onTouch: scale: " + actionWrapper.getCanvasScale() + ", dx: " + actionWrapper.getCanvasTranslateDx() + ", dy: " + actionWrapper.getCanvasTranslateDy());
                if (actionWrapper.getCanvasScale() != 1) {
//                    scale(actionWrapper.getCanvasScale(), actionWrapper.getCanvasScalePointX(), actionWrapper.getCanvasScalePointY(), true);
//                    canvasTransformMatrix.preScale(actionWrapper.getCanvasScale(), actionWrapper.getCanvasScale(), actionWrapper.getCanvasScalePointX(), actionWrapper.getCanvasScalePointY());
                    scale(actionWrapper.getCanvasScale(), actionWrapper.getCanvasScalePointX(), actionWrapper.getCanvasScalePointY(), false);
                    actionWrapper.setNeedInvalidate(true);
                }
                if (actionWrapper.getCanvasTranslateDx() != 0 || actionWrapper.getCanvasTranslateDy() != 0) {
//                    scroll(actionWrapper.getCanvasTranslateDx(), actionWrapper.getCanvasTranslateDy(), false);
//                    canvasTransformMatrix.postTranslate(actionWrapper.getCanvasTranslateDx(), actionWrapper.getCanvasTranslateDy());
                    scroll(actionWrapper.getCanvasTranslateDx(), actionWrapper.getCanvasTranslateDy(), false);
                    actionWrapper.setNeedInvalidate(true);
                }
                if (actionWrapper.isNeedInvalidate())
                    invalidate();
            }
        }

        return true;
    }

    protected void scroll(float dx, float dy, boolean invalidate) {
        canvasTransformMatrix.postTranslate(dx, dy);
        if (invalidate)
            invalidate();
    }

    protected void scale(float scale, float px, float py, boolean invalidate) {
        // 别问为什么是preScale不是postScale，尝试得出的。。。
        canvasTransformMatrix.getValues(transformMatrixValues);

        float oldScale = transformMatrixValues[0];
        float newScale = Math.min(Math.max(DrawViewUtil.CANVAS_MIN_SCALE, scale * oldScale), DrawViewUtil.CANVAS_MAX_SCALE);
        if (newScale != oldScale) {
            canvasTransformMatrix.preScale(newScale / oldScale, newScale / oldScale, px, py);
            if (invalidate)
                invalidate();
            notifyAllScaleListener(newScale, oldScale);
        }
    }

    /**
     * 用于设置当前画笔模式。
     * 如果当前在进行操作，也直接cancel掉了。这么做是因为没有想到更好的实现，特别是选择模式下的一些情况。
     * @param mode 应是DrawView.PAINT_MODE、DrawView.REGION_MODE、DrawView.CHOOSE_MODE中的一种。
     *             若不属于这几种，默认DrawView.CHOOSE_MODE
     */
    public void setMode(int mode) {
        if (this.currentMode == mode)
            return;
        if (null != gestureListener)
            gestureListener.onCancel();

        switch (mode) {
            case PAINT_MODE:
                gestureListener = new PathGestureListener(getContext(), pathPaint);
                break;
            case CHOOSE_MODE:
                gestureListener = new ChooseGestureListener(getContext(), choosePaint);
                break;
            case SCALE_TRANSLATING_MODE:
                gestureListener = new ScaleGestureListener(getContext());
                break;
        }
        this.currentMode = mode;
        invalidate();
    }

    public void redo() {
        if (gestureListener != null)
            gestureListener.onCancel();
        actionHistoryManager.redo();
        invalidate();
    }

    public void undo() {
        if (gestureListener != null)
            gestureListener.onCancel();
        actionHistoryManager.undo();
        invalidate();
    }

    public int getMode() {
        return currentMode;
    }

    public float getPathStrokeWidth() {
        return pathPaint.getStrokeWidth();
    }

    public void setPathStrokeWidth(float pathStrokeWidth) {
        pathPaint.setStrokeWidth(pathStrokeWidth);
    }

    public void setPathColor(int color) {
        pathPaint.setColor(color);
    }

    public int getPathColor() {
        return pathPaint.getColor();
    }

    public void addOnScaleListener(ScaleChangeListener scaleChangeListener) {
        this.scaleChangeListenerSet.add(scaleChangeListener);
    }

    public void notifyAllScaleListener(float newScale, float oldScale) {
        for (ScaleChangeListener scaleChangeListener : this.scaleChangeListenerSet)
            scaleChangeListener.onScaleChanged(newScale, oldScale);

        dashPathEffect[0] = dashPathEffect[1] = DEFAULT_DASH_PATH_EFFECT / newScale;
        choosePaint.setPathEffect(new DashPathEffect(dashPathEffect, 0));
        choosePaint.setStrokeWidth(DEFAULT_CHOOSE_PAINT_STROKE_WIDTH / newScale);
    }

    /**
     * @param bitmap 新增图片元素
     */
    public void addBitmapElement(Bitmap bitmap) {
//        BasicElement element = new BitmapElement(bitmap);
//        elements.add(element);
        actionHistoryManager.addAction(new NewAction(new BitmapElement(bitmap)));
        invalidate();
    }


//    public Paint getPathPaint() {
//        return pathPaint;
//    }
//
//    public Paint getRegionPaint() {
//        return regionPaint;
//    }
//
//    public Paint getChoosePaint() {
//        return choosePaint;
//    }
//
//    public List<BasicElement> getElements() {
//        return actionHistoryManager.getOnCanvasBasicElements();
//    }
//
//    public Drawable getDeleteDrawable() {
//        return deleteDrawable;
//    }
//
//    public Drawable getRotateDrawable() {
//        return rotateDrawable;
//    }
//
//    public Drawable getScaleDrawable() {
//        return scaleDrawable;
//    }

    public ActionHistoryManager getActionHistoryManager() {
        return actionHistoryManager;
    }

    public void setActionHistoryManager(ActionHistoryManager actionHistoryManager) {
        actionHistoryManager.save();
        this.actionHistoryManager = actionHistoryManager;
        invalidate();
    }

}
