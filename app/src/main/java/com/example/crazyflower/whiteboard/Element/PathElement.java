package com.example.crazyflower.whiteboard.Element;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.crazyflower.whiteboard.ParcelablePath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class PathElement extends BasicElement {

    private static final String TAG = "PathElement";

    private ParcelablePath parcelablePath;
    private Paint paint;

    public PathElement(ParcelablePath parcelablePath, Paint paint) {
        super(ElementUtil.PATH_ELEMENT);
        this.parcelablePath = parcelablePath;
        this.paint = paint;
    }

    public PathElement(Parcel source) {
        super(ElementUtil.PATH_ELEMENT, source);

        parcelablePath = source.readParcelable(ParcelablePath.class.getClassLoader());
        paint = new Paint();
        paint.setColor(source.readInt());
        paint.setStrokeWidth(source.readFloat());
        paint.setStyle(Paint.Style.STROKE);
    }

    public PathElement(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        paint = new Paint();
        paint.setColor(jsonObject.getInt("color"));
        paint.setStrokeWidth(jsonObject.getInt("stoke_width"));
        paint.setStyle(Paint.Style.STROKE);
        parcelablePath = new ParcelablePath(jsonObject.getJSONArray("path"));
    }

    @Override
    public void draw(Canvas canvas) {
        ParcelablePath parcelablePath;
        if (chosen) {
            this.paint.setShadowLayer(20, 20, 20, Color.BLACK);
            parcelablePath = new ParcelablePath(this.parcelablePath);
            parcelablePath.transform(super.tempTransformMatrix);
        }
        else {
            this.paint.clearShadowLayer();
            parcelablePath = this.parcelablePath;
        }
        Log.d(TAG, "draw: ");
        canvas.drawPath(parcelablePath.getPath(), paint);
    }

    @Override
    public boolean isCovered(RectF rectF) {
        return rectF.contains(parcelablePath.computeBounds());
    }

    @Override
    public RectF getBound() {
        return parcelablePath.computeBounds();
    }

    @Override
    public void onTransform(Matrix matrix) {
        parcelablePath.transform(matrix);
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("type", super.type);
        result.put("id", uuid.toString());
        result.put("path", parcelablePath.toJSONArray());
        result.put("color", paint.getColor());
        result.put("stoke_width", paint.getStrokeWidth());

        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(parcelablePath, flags);
        dest.writeInt(paint.getColor());
        dest.writeFloat(paint.getStrokeWidth());
    }

}
