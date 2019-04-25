package com.example.crazyflower.whiteboard.Action;

import android.graphics.Matrix;
import android.os.Parcel;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TransformAction extends Action {

    protected List<BasicElement> elements;

    protected Matrix matrix;

    public TransformAction(List<BasicElement> elements, Matrix matrix) {
        super(ACTION_TRANSFORM);
        this.elements = elements;
        this.matrix = matrix;
    }

    public TransformAction(Parcel source, HashMap<UUID, BasicElement> hashMap) {
        super(ACTION_TRANSFORM, source);
        elements = new ArrayList<>();

        int count = source.readInt();
        BasicElement basicElement;
        for (int i = 0; i < count; i++) {
            basicElement = hashMap.get(UUID.fromString(source.readString()));
            if (basicElement != null)
                elements.add(basicElement);
        }

        float[] values = new float[9];
        source.readFloatArray(values);
        matrix = new Matrix();
        matrix.setValues(values);
    }

    public TransformAction(JSONObject jsonObject, HashMap<UUID, BasicElement> hashMap) throws JSONException {
        super(ACTION_TRANSFORM, jsonObject);

        elements = new ArrayList<>();
        JSONArray elementIDs = jsonObject.getJSONArray(JSON_ELEMENT_IDS);
        for (int i = 0, length = elementIDs.length(); i < length; i++)
            elements.add(hashMap.get(UUID.fromString(elementIDs.getString(i))));
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        for (BasicElement element : elements)
            element.onTransform(matrix);
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        Matrix invertMatrix = new Matrix();
        matrix.invert(invertMatrix);
        for (BasicElement element : elements)
            element.onTransform(invertMatrix);
    }

    @Override
    public void writeToJSONObject(JSONObject jsonObject) throws JSONException {
        super.writeToJSONObject(jsonObject);

        JSONArray elementIds = new JSONArray();
        for (BasicElement element : elements)
            elementIds.put(element.getUuid().toString());
        jsonObject.put(JSON_ELEMENT_IDS, elementIds);

        float[] values = new float[9];
        matrix.getValues(values);
        JSONArray matrixValues = new JSONArray();
        for (float value : values)
            matrixValues.put(value);
        jsonObject.put(JSON_MATRIX_VALUES, matrixValues);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(elements.size());
        for (BasicElement element : elements)
            dest.writeString(element.getUuid().toString());

        float[] values = new float[9];
        matrix.getValues(values);
        dest.writeFloatArray(values);
    }
}
