package com.example.crazyflower.whiteboard.Element;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;


public abstract class BasicElement implements Parcelable {

    private static final String TAG = "BasicElement";
    // 是否被选中
    protected boolean chosen;
//    被选中时的中心点
//    protected PointF center;
    // 被选中时的变换矩阵
    protected Matrix tempTransformMatrix;

    protected UUID uuid;

    protected int type;

    BasicElement(int type) {
        this(type, UUID.randomUUID());
    }

    protected BasicElement(int type, UUID uuid) {
        this.type = type;
        chosen = false;
        tempTransformMatrix = null;
        this.uuid = uuid;
    }

    BasicElement(int type, Parcel source) {
        this.type = type;
        chosen = false;
        tempTransformMatrix = null;
        uuid = UUID.fromString(source.readString());
        Log.d(TAG, "BasicElement: "+ uuid.toString());
    }

    BasicElement(JSONObject jsonObject) {
        try {
            type = jsonObject.getInt(ElementUtil.ELEMENT_TYPE);
            uuid = UUID.fromString(jsonObject.getString(ElementUtil.ELEMENT_ID));
            chosen = false;
            tempTransformMatrix = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param canvas
     * Element draw themselves in the canvas.
     */
    public abstract void draw(Canvas canvas);

    public boolean isChosen() {
        return chosen;
    }

    public void cancelChosen() {
        this.chosen = false;
        tempTransformMatrix = null;
    }

    public abstract boolean isCovered(RectF rectF);

    public void setChosen(Matrix tempTransformMatrix) {
        this.chosen = true;
        this.tempTransformMatrix = tempTransformMatrix;
    }

    public UUID getUuid() {
        return uuid;
    }

    public abstract RectF getBound();

    public abstract void onTransform(Matrix matrix);

    public abstract JSONObject toJSONObject() throws JSONException;

    public static final Creator<BasicElement> CREATOR = new Creator<BasicElement>() {

        public static final String TAG = "Creator<BasicElement>";
        @Override
        public BasicElement createFromParcel(Parcel in) {
            int type = in.readInt();
            Log.d(TAG, "createFromParcel: " + type);
            switch (type) {
                case ElementUtil.PATH_ELEMENT:
                    return new PathElement(in);
            }
            return null;
        }

        @Override
        public BasicElement[] newArray(int size) {
            return new BasicElement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(uuid.toString());
    }
}
