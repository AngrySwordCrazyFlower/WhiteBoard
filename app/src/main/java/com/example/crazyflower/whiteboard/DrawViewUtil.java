package com.example.crazyflower.whiteboard;

import android.graphics.RectF;

public class DrawViewUtil {

    public static final float CHOSEN_SCALE_COEFFICIENT = 1.1f;

    /**
     * 根据提供的上下左右四条边的x/y值，返回RectF,确保返回的RectF中，left<=right,top<=bottom。
     * @param left 原始的左边x值
     * @param top 原始的上边y值
     * @param right 原始的右边x值
     * @param bottom 原始的下边y值
     * @return 一个确保left<=right,top<=bottom的RectFx
     */
    public static RectF makeRectF(float left, float top, float right, float bottom) {
        float temp;
        if (left > right) {
            temp = left;
            left = right;
            right = temp;
        }
        if (top > bottom) {
            temp = top;
            top = bottom;
            bottom = temp;
        }
        return new RectF(left, top, right, bottom);
    }

    /**
     * 根据提供的上下左右四条边的x/y值，返回RectF,确保返回的RectF中，left<=right,top<=bottom。
     * @param rectF 记录最终结果的RectF对象
     * @param left 原始的左边x值
     * @param top 原始的上边y值
     * @param right 原始的右边x值
     * @param bottom 原始的下边y值
     */
    public static void makeRectF(RectF rectF, float left, float top, float right, float bottom) {
        float temp;
        if (left > right) {
            temp = left;
            left = right;
            right = temp;
        }
        if (top > bottom) {
            temp = top;
            top = bottom;
            bottom = temp;
        }
        rectF.left = left;
        rectF.top = top;
        rectF.right = right;
        rectF.bottom = bottom;
    }

    protected static final int PAINT_MAX_STROKE_WIDTH = 12;
    protected static final int PAINT_MIN_STROKE_WIDTH = 4;

    protected static final float CANVAS_MIN_SCALE = 1.0f / 3;
    protected static final float CANVAS_MAX_SCALE = 5.0f;

}
