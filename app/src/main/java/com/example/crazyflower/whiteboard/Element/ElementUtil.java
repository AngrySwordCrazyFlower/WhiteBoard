package com.example.crazyflower.whiteboard.Element;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ElementUtil {

    public static final int BITMAP_ELEMENT = 1;

    public static final int PATH_ELEMENT = 2;

    protected static final String ELEMENT_TYPE = "type";

    protected static final String ELEMENT_ID = "id";

    public static BasicElement generateElementByJSONObject(JSONObject jsonObject, File file) {
        BasicElement result = null;
        try {
            switch (jsonObject.getInt(ELEMENT_TYPE)) {
                case PATH_ELEMENT:
                    result = new PathElement(jsonObject);
                    break;
                case BITMAP_ELEMENT:
                    result = new BitmapElement(jsonObject, file);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
