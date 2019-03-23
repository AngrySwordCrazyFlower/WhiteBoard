package com.example.crazyflower.whiteboard.Action;

import android.graphics.Matrix;

import com.example.crazyflower.whiteboard.Element.BasicElement;

import java.util.List;

public class TranslateAction extends Action {

    private List<BasicElement> elements;
    private float dx;
    private float dy;

    public TranslateAction(List<BasicElement> elements, float dx, float dy) {
        super(Action.ACTION_TRANSLATE);
        this.elements = elements;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void redo(List<BasicElement> basicElements) {
        Matrix matrix = new Matrix();
        matrix.postTranslate(dx, dy);
        for (BasicElement element : elements) {
            element.onTransform(matrix);
        }
    }

    @Override
    public void undo(List<BasicElement> basicElements) {
        Matrix matrix = new Matrix();
        matrix.postTranslate(-dx, -dy);
        for (BasicElement element : elements) {
            element.onTransform(matrix);
        }
    }
}
