package com.example.crazyflower.whiteboard.Element;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;


public abstract class BasicElement {

    private static final String TAG = "BasicElement";

    protected boolean chosen;

    protected PointF center;

    BasicElement() {
        chosen = false;
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
    }

    public abstract boolean isCovered(RectF rectF);

    public void setChosen(PointF center) {
        this.chosen = true;
        this.center = center;
    }

    public abstract RectF getBound();

    public abstract void onTransform(Matrix matrix);

}
