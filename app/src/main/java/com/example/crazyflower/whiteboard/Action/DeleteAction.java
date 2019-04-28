package com.example.crazyflower.whiteboard.Action;

import android.os.Parcel;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeleteAction extends Action {

    private List<BasicElement> elements;

    public DeleteAction(List<BasicElement> elements) {
        super(Action.ACTION_DELETE);
        this.elements = elements;
    }

    public DeleteAction(Parcel source, HashMap<UUID, BasicElement> hashMap) {
        super(ACTION_DELETE, source);

        int count = source.readInt();
        BasicElement basicElement;
        for (int i = 0; i < count; i++) {
            basicElement = hashMap.get(UUID.fromString(source.readString()));
            if (basicElement != null)
                elements.add(basicElement);
        }
    }

    public DeleteAction(JSONObject jsonObject, HashMap<UUID, BasicElement> hashMap) throws JSONException {
        super(Action.ACTION_DELETE);
        elements = new ArrayList<>();
        JSONArray elementIDs = jsonObject.getJSONArray(JSON_ELEMENT_IDS);
        for (int i = 0, length = elementIDs.length(); i < length; i++)
            elements.add(hashMap.get(UUID.fromString(elementIDs.getString(i))));
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        basicElements.removeAll(elements);
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        basicElements.addAll(elements);
    }

    @Override
    public void writeToJSONObject(JSONObject jsonObject, File file) throws JSONException {
        super.writeToJSONObject(jsonObject, file);

        JSONArray elementIds = new JSONArray();
        for (BasicElement element : elements)
            elementIds.put(element.getUuid().toString());
        jsonObject.put(JSON_ELEMENT_IDS, elementIds);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(elements.size());
        for (BasicElement element : elements)
            dest.writeString(element.getUuid().toString());
    }
}
