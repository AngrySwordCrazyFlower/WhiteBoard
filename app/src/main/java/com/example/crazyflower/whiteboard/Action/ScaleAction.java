package com.example.crazyflower.whiteboard.Action;

import android.graphics.Matrix;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.List;

public class ScaleAction extends Action {

    private List<BasicElement> elements;
    private float scale;
    private float x;
    private float y;

    public ScaleAction(List<BasicElement> elements, float scale, float x, float y) {
        super(Action.ACTION_ROTATE);
        this.elements = elements;
        this.scale = scale;
        this.x = x;
        this.y = y;
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale, x, y);
        for (BasicElement element : elements) {
            element.onTransform(matrix);
        }
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        Matrix matrix = new Matrix();
        matrix.postScale(1 / scale, 1 / scale, x, y);
        for (BasicElement element : elements) {
            element.onTransform(matrix);
        }
    }
}
