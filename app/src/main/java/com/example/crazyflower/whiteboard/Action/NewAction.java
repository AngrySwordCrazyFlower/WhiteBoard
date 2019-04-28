package com.example.crazyflower.whiteboard.Action;

import android.os.Parcel;

import com.example.crazyflower.whiteboard.Element.BasicElement;
import com.example.crazyflower.whiteboard.Element.ElementUtil;
import com.example.crazyflower.whiteboard.Element.PathElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NewAction extends Action {

    private BasicElement element;

    public NewAction(BasicElement element) {
        super(Action.ACTION_NEW);
        this.element = element;
    }

    public NewAction(JSONObject jsonObject, HashMap<UUID, BasicElement> map, File file) throws JSONException {
        super(ACTION_NEW, jsonObject);
        element = ElementUtil.generateElementByJSONObject(jsonObject.getJSONObject(JSON_ELEMENT), file);
        map.put(element.getUuid(), element);
    }

    public NewAction(Parcel source, HashMap<UUID, BasicElement> hashMap) {
        super(ACTION_NEW, source);
//        element = source.readParcelable(BasicElement.class.getClassLoader());
        element = BasicElement.CREATOR.createFromParcel(source);
        hashMap.put(element.getUuid(), element);
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        basicElements.add(element);
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        basicElements.remove(element);
    }

    @Override
    public void writeToJSONObject(JSONObject jsonObject, File file) throws JSONException {
        super.writeToJSONObject(jsonObject, file);
        JSONObject elementJSON = new JSONObject();
        element.writeToJSONObject(elementJSON, file);
        jsonObject.put(JSON_ELEMENT, elementJSON);
    }

    public BasicElement getElement() {
        return element;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        element.writeToParcel(dest, flags);
//        dest.writeParcelable(element, flags);
    }
}
