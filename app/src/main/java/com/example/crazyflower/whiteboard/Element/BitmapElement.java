package com.example.crazyflower.whiteboard.Element;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.crazyflower.whiteboard.DrawViewUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class BitmapElement extends BasicElement {

    private static final String TAG = "BitmapElement";
    private Bitmap bitmap;
    private Paint paint;
    private Matrix transformMatrix;

    public BitmapElement(Bitmap bitmap) {
        super(ElementUtil.BITMAP_ELEMENT);
        init();
        this.bitmap = bitmap;
    }

    protected BitmapElement(Parcel source) {
        super(ElementUtil.BITMAP_ELEMENT, source);
        init();
        bitmap = source.readParcelable(Bitmap.class.getClassLoader());
    }

    protected BitmapElement(JSONObject jsonObject, File file) {
        super(jsonObject);
        init();
        bitmap = BitmapFactory.decodeFile(file.getPath() + File.separator + uuid);
    }

    protected void init() {
        paint = new Paint();
        transformMatrix = new Matrix();
    }

    @Override
    public void draw(Canvas canvas) {
        Matrix matrix;
        if (chosen) {
            this.paint.setShadowLayer(20, 20, 20, Color.BLACK);
            matrix = new Matrix();
            matrix.postConcat(this.transformMatrix);
            matrix.postConcat(super.tempTransformMatrix);
        }
        else {
            matrix = this.transformMatrix;
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
        transformMatrix.getValues(values);

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
        transformMatrix.getValues(values);

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
        this.transformMatrix.postConcat(matrix);
    }

//    @Override
//    public JSONObject toJSONObject(File file) {
//        return new JSONObject();
//    }

    @Override
    public void writeToJSONObject(JSONObject jsonObject, File file) throws JSONException {
        super.writeToJSONObject(jsonObject, file);
        if (file.exists() && file.isDirectory()) {
            File bitmapFile = new File(file.getPath() + File.separator + uuid.toString());
            if (!bitmapFile.exists()) {
                try {
                    BufferedOutputStream  bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(bitmapFile));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bufferedOutputStream);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(bitmap, flags);
    }

}
