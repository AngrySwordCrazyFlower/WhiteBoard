package com.example.crazyflower.whiteboard.Element;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;

public class PathElement extends BasicElement {

    private static final String TAG = "PathElement";

    private Path path;
    private Paint paint;

    public PathElement(Path path, Paint paint) {
        super();
        this.path = path;
        this.paint = paint;
    }

    @Override
    public void draw(Canvas canvas) {
        Path path;
        if (chosen) {
            this.paint.setShadowLayer(20, 20, 20, Color.BLACK);
            path = new Path(this.path);
            Matrix matrix = new Matrix();
            matrix.postScale(DrawViewUtil.CHOSEN_SCALE_COEFFICIENT, DrawViewUtil.CHOSEN_SCALE_COEFFICIENT, center.x, center.y);
            path.transform(matrix);
        }
        else {
            this.paint.clearShadowLayer();
            path = this.path;
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean isCovered(RectF rectF) {
        RectF bounds = new RectF();
        path.computeBounds(bounds, false);
        return rectF.contains(bounds);
    }

    @Override
    public RectF getBound() {
        RectF result = new RectF();
        path.computeBounds(result, false);
        return result;
    }

    @Override
    public void onTransform(Matrix matrix) {
        path.transform(matrix);
    }

//    @Override
//    public void onTranslate(float dx, float dy) {
//        Matrix matrix = new Matrix();
//        matrix.postTranslate(dx, dy);
//        path.transform(matrix);
//        center.x += dx;
//        center.y += dy;
//    }
//
//    @Override
//    public void onRotate(float degree, float x, float y) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate((float) Math.toDegrees(degree), x, y);
//        path.transform(matrix);
//    }
//
//    @Override
//    public void onScale(float scale, float x, float y) {
//        Matrix matrix = new Matrix();
//        matrix.postScale(scale, scale, x, y);
//        path.transform(matrix);
//    }
}
