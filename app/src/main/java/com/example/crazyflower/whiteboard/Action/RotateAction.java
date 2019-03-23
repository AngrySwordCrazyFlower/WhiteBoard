package com.example.crazyflower.whiteboard.Action;

import android.graphics.Matrix;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.List;

public class RotateAction extends Action {

    private List<BasicElement> elements;
    private float angle;
    private float x;
    private float y;

    public RotateAction(List<BasicElement> elements, float angle, float x, float y) {
        super(Action.ACTION_ROTATE);
        this.elements = elements;
        this.angle = angle;
        this.x = x;
        this.y = y;
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, x, y);
        for (BasicElement element : elements) {
            element.onTransform(matrix);
        }
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-angle, x, y);
        for (BasicElement element : elements) {
            element.onTransform(matrix);
        }
    }
}
