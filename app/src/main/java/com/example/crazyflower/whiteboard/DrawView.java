package com.example.crazyflower.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.crazyflower.whiteboard.Action.ActionHistoryManager;
import com.example.crazyflower.whiteboard.Action.DeleteAction;
import com.example.crazyflower.whiteboard.Action.NewAction;
import com.example.crazyflower.whiteboard.Action.RotateAction;
import com.example.crazyflower.whiteboard.Action.ScaleAction;
import com.example.crazyflower.whiteboard.Action.TranslateAction;
import com.example.crazyflower.whiteboard.Element.BasicElement;
import com.example.crazyflower.whiteboard.Element.BitmapElement;
import com.example.crazyflower.whiteboard.Element.PathElement;
import com.example.crazyflower.whiteboard.GestureListener.ChooseGestureListener;
import com.example.crazyflower.whiteboard.GestureListener.GestureListener;
import com.example.crazyflower.whiteboard.GestureListener.PathGestureListener;
import com.example.crazyflower.whiteboard.GestureListener.RectFGestureListener;
import com.example.crazyflower.whiteboard.GestureListener.ScaleGestureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DrawView extends View implements View.OnTouchListener {

    public interface OnScaleListener {

        /**
         *
         * Called when the scale of DrawView changed.
         *
         * @param scale The new scale factor.
         * @param oldScale The old scale factor.
         */
        void onCanvasScaleChanged(float scale, float oldScale);

    }

    public static final int PAINT_MODE = 0x100001;

    public static final int CHOOSE_MODE = 0x100002;

    public static final int REGION_MODE = 0x100003;

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
     * 绘制矩形的画笔
     */
    private Paint regionPaint;

    /**
     * 已经在画布上存在的元素，如果当前正在画，当前那个元素是不在列表中的。
     */
    private List<BasicElement> elements;

    /**
     * 当前画布的偏移量
     */
    private float offsetX;
    private float offsetY;

    /**
     * 当前画布的缩放倍数
     */
    private float scale;

    /**
     * 绘制选择区域的画笔
     */
    private Paint choosePaint;

    /**
     * 被选中区域的删除、旋转、缩放操作的按钮背景图
     */
    private final Drawable deleteDrawable;
    private final Drawable rotateDrawable;
    private final Drawable scaleDrawable;

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
    protected Set<OnScaleListener> onScaleListenerSet;

    public DrawView(Context context) {
        this(context, null);
        Log.d(TAG, "DrawView: " + getWidth() + " " + getHeight());
    }

    public DrawView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        elements = new ArrayList<BasicElement>();

        pathPaint = new Paint();
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(DrawViewUtil.PAINT_MIN_STROKE_WIDTH);
        pathPaint.setColor(Color.rgb(0, 0, 0));

        regionPaint = new Paint();
        regionPaint.setStyle(Paint.Style.STROKE);
        regionPaint.setColor(Color.rgb(0, 0, 0));
        regionPaint.setStrokeWidth(6);

        choosePaint = new Paint();
        choosePaint.setStyle(Paint.Style.STROKE);
        choosePaint.setStrokeWidth(6);
        choosePaint.setColor(Color.rgb(0, 0, 0));
        choosePaint.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));

        scale = 1;

        offsetX = 0;
        offsetY = 0;

        deleteDrawable = getResources().getDrawable(R.drawable.close);
        rotateDrawable = getResources().getDrawable(R.drawable.rotate);
        scaleDrawable = getResources().getDrawable(R.drawable.scale);

        actionHistoryManager = new ActionHistoryManager();

        // 对该View关闭硬件加速，不关闭的话有些地方可能绘制的结果不一样
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        setOnTouchListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*
         * 初始的时候，画布的原点位于View的中心点，所以是w / 2, h / 2
         * 在后来大小改变的时候，offsetX、offsetY已经改变了，应该减去上次的偏移量，加上这次的偏移量，以使得用户进行的拖动没有被影响
         */
        offsetX = offsetX + w / 2.0f - oldw / 2.0f;
        offsetY = offsetY + h / 2.0f - oldh / 2.0f;
    }

    private void drawHistory(Canvas canvas) {
        for (BasicElement object : elements) {
            object.draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 根据当前画布的偏移、缩放进行调整
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);

        // 绘制已经画了的一些元素
        drawHistory(canvas);

        if (null != gestureListener)
            gestureListener.onDraw(canvas);
    }

    /**
     * 将触摸事件的坐标转换为画布上的坐标。
     * @param event 包含需要转换的坐标的事件
     * @return 一个二维坐标点，以PointF对象的形式。first表示转换后坐标的x，second表示转换后坐标的y。
     */
    private PointF transformEventCoordinateToCanvas(MotionEvent event) {
        return new PointF((event.getX() - offsetX) / scale, (event.getY() - offsetY) / scale);
    }

    /**
     * 将触摸事件的坐标转换为画布上的坐标。
     * @param event 包含被转换坐标的事件
     * @param pointF 存放被转换后的坐标
     */
    public void transformEventCoordinateToCanvas(MotionEvent event, PointF pointF) {
        pointF.x = (event.getX() - offsetX) / scale;
        pointF.y = (event.getY() - offsetY) / scale;
    }

    /**
     * 将原始坐标转换为画布上的坐标。
     * @param pointF 存放被转换后的坐标
     */
    public void transformEventCoordinateToCanvas(PointF pointF) {
        pointF.x = (pointF.x - offsetX) / scale;
        pointF.y = (pointF.y - offsetY) / scale;
    }

    /**
     * 将原始长度（例如触摸事件的屏幕坐标相减得到的长度）转换为画布上的坐标。
     */
    public float transformEventDistanceToCanvas(float distance) {
        return distance;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (null != gestureListener)
            return gestureListener.onTouch(event);
        return false;
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
                gestureListener = new PathGestureListener(this);
                break;
            case REGION_MODE:
                gestureListener = new RectFGestureListener(this);
                break;
            case CHOOSE_MODE:
                gestureListener = new ChooseGestureListener(this);
                break;
            case SCALE_TRANSLATING_MODE:
                gestureListener = new ScaleGestureListener(this);
                break;
        }
        this.currentMode = mode;
        invalidate();
    }

    public void redo() {
        gestureListener.onCancel();
        actionHistoryManager.redo(elements);
        invalidate();
    }

    public void undo() {
        gestureListener.onCancel();
        actionHistoryManager.undo(elements);
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

    /**
     * 在一些模式下，画布缩放的时候，调用该函数。
     * 参数中的scale，是缩放倍数，但并不一定采用这个倍数，因为画布有最大和最小缩放倍数。
     * @param scale 缩放倍数
     * @param x 缩放中心点横坐标
     * @param y 缩放中心点纵坐标
     */
    public void onCanvasScale(float scale, float x, float y) {
        scale = setScale(this.scale * scale);
        offsetX += x * (1 - scale);
        offsetY += y * (1 - scale);
        invalidate();
    }

    /**
     * 在一些模式下，画布移动的时候，调用该函数。
     * @param dx 水平方向移动的量（单位：像素）
     * @param dy 垂直方向移动的量（单位：像素）
     */
    public void onCanvasTranslate(float dx, float dy) {
        offsetX += dx;
        offsetY += dy;
        this.invalidate();
    }

    public void addOnScaleListener(OnScaleListener onScaleListener) {
        this.onScaleListenerSet.add(onScaleListener);
    }

    public void notifyAl(float newScale, float oldScale) {
        for (OnScaleListener onScaleListener : this.onScaleListenerSet)
            onScaleListener.onCanvasScaleChanged(newScale, oldScale);
    }

    /**
     * @param scale 想要缩放的倍数
     * @return 实际在原基础上缩放的倍数
     */
    public float setScale(float scale) {
        scale = Math.min(Math.max(scale, DrawViewUtil.CANVAS_MIN_SCALE), DrawViewUtil.CANVAS_MAX_SCALE);

        float factor = scale / this.scale;
        this.scale = scale;
        this.choosePaint.setStrokeWidth(6 / this.scale);
        choosePaint.setPathEffect(new DashPathEffect(new float[] {5 / this.scale, 5 / this.scale}, 0));

        return factor;
    }

    /**
     * @param path 新增画笔元素
     */
    public void onPathEnd(Path path) {
        BasicElement element = new PathElement(new Path(path), new Paint(pathPaint));
        elements.add(element);
        actionHistoryManager.addAction(new NewAction(element));
        invalidate();
    }

    /**
     * @param rectF 新增矩形元素（实际上也是通过path）
     */
    public void onRectFEnd(RectF rectF) {
        Path path = new Path();
        path.addRect(rectF.left, rectF.top, rectF.right, rectF.bottom, Path.Direction.CCW);
        BasicElement element = new PathElement(path, new Paint(regionPaint));
        elements.add(element);
        actionHistoryManager.addAction(new NewAction(element));
        invalidate();
    }

    /**
     * @param bitmap 新增图片元素
     */
    public void addBitmapElement(Bitmap bitmap) {
        BasicElement element = new BitmapElement(bitmap);
        elements.add(element);
        actionHistoryManager.addAction(new NewAction(element));
        invalidate();
    }

    /**
     * 某些元素被平移的时候调用
     * @param elements 被选中的元素
     * @param dx 水平方向偏移量（单位：像素）
     * @param dy 垂直方向偏移量（单位：像素）
     */
    public void onChooseTranslateEnd(List<BasicElement> elements, float dx, float dy) {
        actionHistoryManager.addAction(new TranslateAction(elements, dx, dy));
    }

    /**
     * 某些元素被旋转的时候调用
     * @param elements 被选中的元素
     * @param rotate 旋转角度（角度制， > 0 逆时针）（前面括号里的应该没记错）
     * @param x 缩放中心点在画布上的横坐标
     * @param y 缩放中心点在画布上的纵坐标
     */
    public void onChooseRotateEnd(List<BasicElement> elements, float rotate, float x, float y) {
        actionHistoryManager.addAction(new RotateAction(elements, rotate, x, y));
    }

    /**
     * 某些元素被缩放的时候调用
     * @param elements 被选中的元素
     * @param scale 实际缩放倍数
     * @param x 缩放中心点在画布上的横坐标
     * @param y 缩放中心点在画布上的纵坐标
     */
    public void onChooseScaleEnd(List<BasicElement> elements, float scale, float x, float y) {
        actionHistoryManager.addAction(new ScaleAction(elements, scale, x, y));
    }

    /**
     * 某些元素被删除的时候调用
     * @param elements 被选中的元素
     */
    public void onChooseElementsDelete(List<BasicElement> elements) {
        this.elements.removeAll(elements);
        actionHistoryManager.addAction(new DeleteAction(elements));
    }

    public Paint getPathPaint() {
        return pathPaint;
    }

    public Paint getRegionPaint() {
        return regionPaint;
    }

    public Paint getChoosePaint() {
        return choosePaint;
    }

    public List<BasicElement> getElements() {
        return elements;
    }

    public Drawable getDeleteDrawable() {
        return deleteDrawable;
    }

    public Drawable getRotateDrawable() {
        return rotateDrawable;
    }

    public Drawable getScaleDrawable() {
        return scaleDrawable;
    }

}
