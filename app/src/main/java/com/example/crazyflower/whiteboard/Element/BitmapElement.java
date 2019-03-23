package com.example.crazyflower.whiteboard.Element;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.example.crazyflower.whiteboard.DrawViewUtil;

public class BitmapElement extends BasicElement {

    private static final String TAG = "BitmapElement";
    private Bitmap bitmap;
    private Paint paint;
    private Matrix matrix;

    public BitmapElement(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.paint = new Paint();
        matrix = new Matrix();
    }

    @Override
    public void draw(Canvas canvas) {
        Matrix matrix;
        if (chosen) {
            this.paint.setShadowLayer(20, 20, 20, Color.BLACK);
            matrix = new Matrix(this.matrix);
            matrix.postScale(DrawViewUtil.CHOSEN_SCALE_COEFFICIENT, DrawViewUtil.CHOSEN_SCALE_COEFFICIENT, center.x, center.y);
        }
        else {
            matrix = this.matrix;
            this.paint.clearShadowLayer();
        }
        canvas.drawBitmap(bitmap, matrix, paint);
    }

    public static final int[][] COEFFICIENT = new int[][] {{0, 0}, {0, 1}, {1, 0}, {1, 1}};

    @Override
    public boolean isCovered(RectF rectF) {
        float[] values = new float[9];
        float halfWidth = bitmap.getWidth();
        float halfHeight = bitmap.getHeight();
        matrix.getValues(values);

        float x, y, transformX, transformY;

        for (int[] aCOEFFICIENT : COEFFICIENT) {
            x = aCOEFFICIENT[0] * halfWidth;
            y = aCOEFFICIENT[1] * halfHeight;
            transformX = values[0] * x + values[1] * y + values[2];
            transformY = values[3] * x + values[4] * y + values[5];
            Log.d(TAG, "isCovered: " + transformX + "  " + transformY);
            if (!rectF.contains(transformX, transformY))
                return false;
        }

        return true;
    }


    @Override
    public RectF getBound() {
        float[] values = new float[9];
        float halfWidth = bitmap.getWidth();
        float halfHeight = bitmap.getHeight();
        matrix.getValues(values);

        float x, y, transformX, transformY;

        RectF rectF = null;

        for (int[] aCOEFFICIENT : COEFFICIENT) {
            x = aCOEFFICIENT[0] * halfWidth;
            y = aCOEFFICIENT[1] * halfHeight;
            transformX = values[0] * x + values[1] * y + values[2];
            transformY = values[3] * x + values[4] * y + values[5];
            if (null == rectF) {
                rectF = new RectF(transformX, transformY, transformX, transformY);
            } else {
                rectF.left = Math.min(rectF.left, transformX);
                rectF.right = Math.max(rectF.right, transformX);
                rectF.top = Math.min(rectF.top, transformY);
                rectF.bottom = Math.max(rectF.bottom, transformY);
            }
        }
        return rectF;
    }

    @Override
    public void onTransform(Matrix matrix) {
        this.matrix.postConcat(matrix);
    }



}
