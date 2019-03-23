package com.example.crazyflower.whiteboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * 是否改为继承自FrameLayout，且进行相应修改，待考虑
 */
public class DragHelperView extends android.support.v7.widget.AppCompatImageView implements View.OnTouchListener {

    /**
     * 监听者的接口
     */
    public interface DragHelperViewListener {
        /**
         * 当DragView点击拖动之后调用
         * @param dx 水平方向上的移动，>0 向右
         * @param dy 垂直方向上的移动，>0 向下
         */
        void onMove(float dx, float dy);
    }

    private static final String TAG = "DrawHelperView";

    protected int width;
    protected int height;

    /**
     * 记录ACTION_DOWN的手指ID
     */
    protected int downPointerID;

    /**
     * 记录上次ACTION_DOWN的时间
     */
    protected long lastPointerDownTime;

    /**
     * 记录上次ACTION_UP的时间
     */
    protected long lastPointerUpTime;

    /**
     * 上次动作从ACTION_DOWN开始到ACTION_UP是否为点击
     */
    protected boolean isLastTouchSingleClick;

    /**
     * 上次事件的屏幕坐标
     */
    protected PointF lastCoordinate;

    /**
     * MOVE动作的监听者
     */
    protected DragHelperViewListener dragHelperViewListener;

    /**
     * 记录当前的状态
     */
    protected int state;

    protected static final int NO_STATE = 0;

    /**
     * MOVE_STATE 手指移动会回调监听者。
     */
    protected static final int MOVE_STATE_START = 1;
    protected static final int MOVE_STATE_SCROLL = 2;

    /**
     * DRAG_STATE 手指移动会拖动该控件。
     */
    protected static final int DRAG_STATE = 10;

    /**
     * 该View距离父容器的最小边距
     */
    protected float minLeftMargin;
    protected float minTopMargin;
    protected float minRightMargin;
    protected float minBottomMargin;

    /**
     * 靠近某边时，小于该临界值则自动紧贴，也是拉离紧贴一边所需要的一次性位移的临界值。单位是像素。
     */
    protected static final int CRITICAL = 16;

    public DragHelperView(Context context) {
        this(context, null);
    }

    public DragHelperView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragHelperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragHelperView);
        setMinMargin(typedArray.getDimension(R.styleable.DragHelperView_minMargin, 0));
        typedArray.recycle();

        state = NO_STATE;
        lastCoordinate = new PointF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * 在这个方法中，调用状态设置setPress(),setActivated()，是为了StateListAnimator的使用，也算是符合Google提倡的一些规范吧
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean result = false;
        Log.d(TAG, "onTouch: " + event.getEventTime());
        long temp;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 记录ACTION_DOWN事件的手指ID
                downPointerID = event.getPointerId(event.getActionIndex());
                temp = event.getDownTime();
                // 如果上次是点击事件且这次按下与上次抬起间隔较小，则判断为拖动该View，否则是为了拖动DrawView的画布
                if (isLastTouchSingleClick && temp - lastPointerUpTime <= 500) {
                    setActivated(true);
                    state = DRAG_STATE;
                } else {
                    state = MOVE_STATE_START;
                    lastPointerDownTime = temp;
                }
                lastCoordinate.x = event.getRawX();
                lastCoordinate.y = event.getRawY();
                result = true;
                setPressed(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (downPointerID == event.getPointerId(event.getActionIndex()) && NO_STATE != state) {
                    if (MOVE_STATE_START == state)
                        state = MOVE_STATE_SCROLL;
                    if (MOVE_STATE_SCROLL == state) { // MOVE状态拖动
                        onMove(event.getRawX() - lastCoordinate.x, event.getRawY() - lastCoordinate.y);
                    } else { // DRAG状态拖动
                        onDrag(event.getRawX() - lastCoordinate.x, event.getRawY() - lastCoordinate.y);
                    }
                    lastCoordinate.x = event.getRawX();
                    lastCoordinate.y = event.getRawY();
                    result = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (downPointerID == event.getPointerId(event.getActionIndex()) && NO_STATE != state) {
                    lastPointerUpTime = event.getEventTime();
                    // 判断这次的完整事件是否为快速单击事件
                    isLastTouchSingleClick = MOVE_STATE_START == state && (lastPointerUpTime - lastPointerDownTime) <= 500;
                    state = NO_STATE;
                    setPressed(false);
                    setActivated(false);
                    result = true;
                }
                break;
        }
        return result;
    }

    protected void onMove(float dx, float dy) {
        Log.d(TAG, "onMove: " + dx + " " + dy);
        if (null != dragHelperViewListener)
            dragHelperViewListener.onMove(dx, dy);
    }

    /**
     * 当该View被拖动的时候，计算相应的Margin，并且要处于规定范围内
     * @param dx 水平方向偏移量, > 0 向右
     * @param dy 垂直方向偏移量，> 0 向下
     */
    protected void onDrag(float dx, float dy) {
        Log.d(TAG, "onDrag: ");

        float leftMargin, bottomMargin;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) layoutParams;
            ViewGroup parent = (ViewGroup) getParent();
            leftMargin = relativeLayoutParams.leftMargin + dx;
            bottomMargin = relativeLayoutParams.bottomMargin - dy;

            leftMargin = Math.max(this.minLeftMargin, Math.min(leftMargin, parent.getWidth() - minRightMargin - width));
            bottomMargin = Math.max(this.minBottomMargin, Math.min(bottomMargin, parent.getHeight() - minTopMargin - height));

            leftMargin = leftMargin - this.minLeftMargin <= CRITICAL ? this.minLeftMargin : leftMargin;
            leftMargin = parent.getWidth() - minRightMargin - width - leftMargin <= CRITICAL ? parent.getWidth() - minRightMargin - width : leftMargin;
            bottomMargin = bottomMargin - this.minBottomMargin <= CRITICAL ? this.minBottomMargin : bottomMargin;
            bottomMargin = parent.getHeight() - minTopMargin - height - bottomMargin <= CRITICAL ? parent.getHeight() - minTopMargin - height : bottomMargin;

            relativeLayoutParams.leftMargin = (int) leftMargin;
            relativeLayoutParams.bottomMargin = (int) bottomMargin;
        } else if (layoutParams instanceof ConstraintLayout.LayoutParams) {
            //有待完善
        }
        requestLayout();
    }

    public void setDragHelperViewListener(DragHelperViewListener dragHelperViewListener) {
        this.dragHelperViewListener = dragHelperViewListener;
    }

    public void setMinMargin(float margin) {
        setMinMargin(margin, margin, margin, margin);
    }

    public void setMinMargin(float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        this.minLeftMargin = leftMargin;
        this.minTopMargin = topMargin;
        this.minRightMargin = rightMargin;
        this.minBottomMargin = bottomMargin;
    }

}
