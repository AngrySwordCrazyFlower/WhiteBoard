package com.example.crazyflower.whiteboard;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ParcelablePath implements Parcelable {

    private static final String TAG = "ParcelablePath";
    protected Path path;

    protected List<PointF> pointFs;

    RectF rectF;

    public ParcelablePath() {
        path = new Path();
        pointFs = new ArrayList<>();
    }

    public ParcelablePath(ParcelablePath parcelablePath) {
        path = new Path(parcelablePath.getPath());
        pointFs = new ArrayList<>(parcelablePath.pointFs);
    }

    public ParcelablePath(JSONArray jsonArray) {
        this();
        float x, y;
        try {
            for (int i = 0, length = jsonArray.length(); i + 1 < length; i = i + 2) {
                x = (float) jsonArray.getDouble(i);
                y = (float) jsonArray.getDouble(i + 1);
                lineTo(x, y);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected ParcelablePath(Parcel in) {
        List<PointF> pointFs = in.createTypedArrayList(PointF.CREATOR);
        this.pointFs = new ArrayList<>();
        this.path = new Path();
        PointF pointF;
        for (int i = 0, length = pointFs.size(); i < length; i++) {
            pointF = pointFs.get(i);
            lineTo(pointF.x, pointF.y);
            Log.d(TAG, "ParcelablePath: " + pointF.x + " " + pointF.y);
        }
    }

    public static final Creator<ParcelablePath> CREATOR = new Creator<ParcelablePath>() {
        @Override
        public ParcelablePath createFromParcel(Parcel in) {
            return new ParcelablePath(in);
        }

        @Override
        public ParcelablePath[] newArray(int size) {
            return new ParcelablePath[size];
        }
    };

    public void moveTo(float x, float y) {
        reset();
        path.moveTo(x, y);
        pointFs.add(new PointF(x, y));
    }

    public void reset() {
        path.reset();
        pointFs.clear();
    }

    public void lineTo(float x, float y) {
        if (pointFs.size() == 0)
            moveTo(x, y);
        else {
            path.lineTo(x, y);
            pointFs.add(new PointF(x, y));
        }
    }

    public void transform(Matrix matrix) {
        path.transform(matrix);
    }

    public RectF computeBounds() {
        if (rectF == null)
            rectF = new RectF();
        path.computeBounds(rectF, false);
        return rectF;
    }

    public Path getPath() {
        StringBuilder stringBuilder = new StringBuilder();
        for (PointF pointF : pointFs)
            stringBuilder.append(pointF.x).append(' ').append(pointF.y).append(' ');
        Log.d(TAG, "getPath: " + stringBuilder.toString());
        return path;
    }

    public JSONArray toJSONArray() {
        JSONArray pointFsArray = new JSONArray();
        try {
            for (PointF pointF : pointFs)
                pointFsArray.put(pointF.x).put(pointF.y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pointFsArray;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(pointFs);
    }

}
