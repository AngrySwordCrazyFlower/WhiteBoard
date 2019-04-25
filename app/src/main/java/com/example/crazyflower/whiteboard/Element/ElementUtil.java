package com.example.crazyflower.whiteboard.Element;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

public class ElementUtil {

    public static final int BITMAP_ELEMENT = 1;

    public static final int PATH_ELEMENT = 2;

    protected static final String ELEMENT_TYPE = "type";

    protected static final String ELEMENT_ID = "id";

    public static BasicElement generateElementByJSONObject(JSONObject jsonObject) {
        BasicElement result = null;
        try {
            switch (jsonObject.getInt(ELEMENT_TYPE)) {
                case PATH_ELEMENT:
                    result = new PathElement(jsonObject);
                    break;
                case BITMAP_ELEMENT:
                    result = new BitmapElement(jsonObject);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
