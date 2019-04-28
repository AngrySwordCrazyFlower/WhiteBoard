package com.example.crazyflower.whiteboard.Action;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class Action implements Parcelable {

    private static final String TAG = "Action";
    /**
     * 动作Id,暂时没有用，未来图画的本地化可能会用到。
     */
    private UUID id;

    /**
     * 动作类型，应该是ACTION_NEW、ACTION_TRANSLATE、ACTION_ROTATE、ACTION_DELETE中的一种，未来可能有新的种类。
     */
    protected int type;

    /**
     * 新增元素动作类型值
     */
    protected final static int ACTION_NEW = 0;

    /**
     * 移动元素动作类型值
     */
    protected final static int ACTION_TRANSFORM = 1;

    /**
     * 删除元素动作类型值
     */
    protected final static int ACTION_DELETE = 2;

    protected static final String JSON_TYPE = "type";

    protected static final String JSON_ID = "id";

    protected static final String JSON_ELEMENT_IDS = "element_id";

    protected static final String JSON_ELEMENT = "element";

    protected static final String JSON_MATRIX_VALUES = "element";

    protected Action(int type) {
        this.type = type;
        this.id = UUID.randomUUID();
        Log.d(TAG, "Action: " + id.toString());
    }

    protected Action(int type, JSONObject jsonObject) throws JSONException {
        this.type = type;
        this.id = UUID.fromString(jsonObject.getString(JSON_ID));
    }

    protected Action(int type, Parcel source) {
        this.type = type;
        id = UUID.fromString(source.readString());
    }

    /**
     * 恢复。根据该动作的记录，重做该动作。
     * @param basicElements 用于进行操作的元素列表，比如新增一个元素，则该元素应该新增在这个列表中。
     */
    public abstract void redo(List<BasicElement> basicElements);

    /**
     * 撤销。根据该动作的记录，撤销该动作。
     * @param basicElements 用于进行操作的元素列表，比如新增一个元素，则该元素应该从这个列表中删除。
     */
    public abstract void undo(List<BasicElement> basicElements);

    public static JSONObject toJSONObject(Action action, File file) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        action.writeToJSONObject(jsonObject, file);
        return jsonObject;
    }

    public void writeToJSONObject(JSONObject jsonObject, File file) throws JSONException {
        jsonObject.put(JSON_TYPE, type);
        jsonObject.put(JSON_ID, id.toString());
    }

    public static Action generateByJSONObject(JSONObject jsonObject, HashMap<UUID, BasicElement> map, File file) {
        Action result = null;
        try {
            switch (jsonObject.getInt("type")) {
                case ACTION_NEW:
                    result = new NewAction(jsonObject, map, file);
                    break;
                case ACTION_DELETE:
                    result = new DeleteAction(jsonObject, map);
                    break;
                case ACTION_TRANSFORM:
                    result = new TransformAction(jsonObject, map);
                    break;
            }
        } catch (JSONException e) {
            result = null;
        }
        return result;
    }

    public static Action createFromParcel(Parcel source, HashMap<UUID, BasicElement> hashMap) {
        int type = source.readInt();
        Log.d(TAG, "createFromParcel: " + type);
        Action result = null;
        switch (type) {
            case ACTION_NEW:
                result = new NewAction(source, hashMap);
                break;
            case ACTION_DELETE:
                result = new DeleteAction(source, hashMap);
                break;
            case ACTION_TRANSFORM:
                result = new TransformAction(source, hashMap);
                break;
        }
        return result;
    }

//    public static final Creator<Action> CREATOR = new Creator<Action>() {
//
//        public static final String TAG = "Creator<Action>";
//
//        @Override
//        public Action createFromParcel(Parcel source) {
//            int type = source.readInt();
//            Log.d(TAG, "createFromParcel: " + type);
//            switch (type) {
//                case ACTION_NEW:
//                    return new NewAction(source);
//            }
//            return null;
//        }
//
//        @Override
//        public Action[] newArray(int size) {
//            return new Action[0];
//        }
//    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(id.toString());
    }
}
