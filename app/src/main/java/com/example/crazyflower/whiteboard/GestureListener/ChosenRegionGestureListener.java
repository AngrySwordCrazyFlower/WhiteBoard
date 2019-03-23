package com.example.crazyflower.whiteboard.GestureListener;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import com.example.crazyflower.whiteboard.DrawView;
import com.example.crazyflower.whiteboard.DrawViewUtil;
import com.example.crazyflower.whiteboard.Element.BasicElement;
import com.example.crazyflower.whiteboard.Vector2D;

import java.lang.ref.WeakReference;
import java.util.List;


public class ChosenRegionGestureListener extends GestureListener {

    private List<BasicElement> chosenElements;

    private int downPointerId;

    private PointF downPointF;
    private PointF scrollPointF;

    private PointF leftTop;
    private PointF leftBottom;
    private PointF rightTop;
    private PointF rightBottom;

    private float dx;
    private float dy;

    protected float scale;

    protected float rotate;

//    private int state;

    public static final int NO_STATE = 0;
    public static final int SCALING_STATE = 1;
    public static final int ROTATING_STATE = 2;
    public static final int TRANSLATING_STATE = 3;
    public static final int DELETING_STATE = 4;

//    private final static int DELETE_NO_STATE = 0;
//    private final static int DELETE_DOWN_STATE = 1;

    private int state;

    private static final String TAG = "ChosenRegionGL";

    private PointF center;

    private WeakReference<ChooseGestureListener> chooseGestureListenerWeakReference;

//    public ChosenRegionGestureListener(DrawView drawView, List<BasicElement> chosenElements) {
//        this()
//        super(drawView);
//    }

    public ChosenRegionGestureListener(DrawView drawView, ChooseGestureListener chooseGestureListener, List<BasicElement> chosenElements) {
//        this(rectF.left, rectF.top, rectF.right, rectF.bottom, chosenElements, deleteDrawable, rotateDrawable, scaleDrawable);
        super(drawView);
        this.chosenElements = chosenElements;
        downPointF = new PointF();
        scrollPointF = new PointF();
        state = NO_STATE;

        Region region = new Region();
        Rect dst = new Rect();
        for (BasicElement basicElement : chosenElements) {
            basicElement.getBound().roundOut(dst);
            region.op(dst, Region.Op.UNION);
        }

        Rect bound = region.getBounds();

        float left = bound.left, top = bound.top, right = bound.right, bottom = bound.bottom;
        float centerX = (left + right) * 0.5f;
        float centerY = (top + bottom) * 0.5f;

        center = new PointF(centerX, centerY);
        for (BasicElement basicElement : chosenElements)
            basicElement.setChosen(center);

        left = centerX + (left - centerX) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
        right = centerX + (right - centerX) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
        top = centerY + (top - centerY) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
        bottom = centerY + (bottom - centerY) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;

        leftTop = new PointF(left, top);
        leftBottom = new PointF(left, bottom);
        rightTop = new PointF(right, top);
        rightBottom = new PointF(right, bottom);

        leftTop = new PointF(bound.left, bound.top);
        rightTop = new PointF(bound.right, bound.top);
        rightBottom = new PointF(bound.right, bound.bottom);
        leftBottom = new PointF(bound.left, bound.bottom);

        this.chosenElements = chosenElements;

        chooseGestureListenerWeakReference = new WeakReference<ChooseGestureListener>(chooseGestureListener);

    }

//    public ChosenRegionGestureListener(float left, float top, float right, float bottom, List<BasicElement> chosenElements, Drawable deleteDrawable, Drawable rotateDrawable, Drawable scaleDrawable) {
//        this.chosenElements = chosenElements;
//        downPointF = new PointF();
//        scrollPointF = new PointF();
//        state = NO_STATE;
//
//        float centerX = (left + right) * 0.5f;
//        float centerY = (top + bottom) * 0.5f;
//        left = centerX - (centerX - left) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        right = centerX + (right - centerX) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        top = centerY - (centerY - top) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        bottom = centerY + (bottom - centerY) * DrawViewUtil.CHOSEN_SCALE_COEFFICIENT;
//        leftTop = new PointF(left, top);
//        leftBottom = new PointF(left, bottom);
//        rightTop = new PointF(right, top);
//        rightBottom = new PointF(right, bottom);
//
//
//        this.state = NO_STATE;
//        this.canvasScale = 1;
//    }

    public boolean onTouch(MotionEvent motionEvent) {
//        Log.d(TAG, "onTouch: ");
        DrawView drawView = getDrawView();
        if (null == drawView)
            return false;
        boolean result = false;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (NO_STATE == state) {
                    downPointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    drawView.transformEventCoordinateToCanvas(motionEvent, downPointF);
                    if (isPressScale(downPointF.x, downPointF.y)) {
                        startScaling();
                        result = true;
                    } else if (isPressRotate(downPointF.x, downPointF.y)) {
                        startRotating();
                        result = true;
                    } else if (isPressDelete(downPointF.x, downPointF.y)) {
                        startDeleting();
                        result = true;
                    } else if (isInRect(downPointF)) {
                        startTranslating();
                        result = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (NO_STATE != state) {
                        drawView.transformEventCoordinateToCanvas(motionEvent, scrollPointF);
                        onScroll();
                        result = true;
                        drawView.invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (downPointerId == motionEvent.getPointerId(motionEvent.getActionIndex())) {
                    if (NO_STATE != state) {
                        onScrollEnd();
                        result = true;
                        drawView.invalidate();
                    }
                }
                break;

        }
        return result;
    }

//    @Override
//    public void onDraw(Canvas canvas) {
//        Log.d(TAG, "onDraw: ");
//        chooseElement.draw(canvas);
//    }

    private void onScroll() {
        switch (state) {
            case SCALING_STATE:
                scaling(scrollPointF.x, scrollPointF.y);
                break;
            case ROTATING_STATE:
                rotating(scrollPointF);
                break;
            case TRANSLATING_STATE:
                translating(scrollPointF.x - downPointF.x, scrollPointF.y - downPointF.y);
                downPointF.x = scrollPointF.x;
                downPointF.y = scrollPointF.y;
                break;
        }
    }

    private void onScrollEnd() {
        DrawView drawView = getDrawView();
        if (null == drawView)
            return;
        switch (state) {
            case SCALING_STATE:
                drawView.onChooseScaleEnd(chosenElements, scale, centerX(), centerY());
                endScaling();
                break;
            case ROTATING_STATE:
                drawView.onChooseRotateEnd(chosenElements, (float) Math.toDegrees(rotate), centerX(), centerY());
                endRotating();
                break;
            case TRANSLATING_STATE:
                drawView.onChooseTranslateEnd(chosenElements, dx, dy);
                endTranslating();
                break;
            case DELETING_STATE:
                ChooseGestureListener chooseGestureListener = chooseGestureListenerWeakReference.get();
                if (null != chooseGestureListener)
                    chooseGestureListener.onChosenElementsDelete();
                endDeleting();
                break;
        }
    }

    @Override
    public void onCancel() {
        for (BasicElement element : chosenElements)
            element.cancelChosen();
        state = NO_STATE;
    }

    public void onDraw(Canvas canvas) {
        Log.d(TAG, "draw: " + leftTop.x + " " + leftTop.y);
        DrawView drawView = getDrawView();
        if (null == drawView)
            return;
        int width = 100, height = 100;

        Drawable deleteDrawable = drawView.getDeleteDrawable();
        Drawable rotateDrawable = drawView.getRotateDrawable();
        Drawable scaleDrawable = drawView.getScaleDrawable();

        // draw close icon at top-right.
//        width = (int) (deleteDrawable.getIntrinsicWidth() / scale);
//        height = (int) (deleteDrawable.getIntrinsicHeight() / scale);
        Log.d(TAG, "draw: " + width + " " + height);
        deleteDrawable.setBounds((int) (rightTop.x - width * 0.5), (int) (rightTop.y - height * 0.5), (int) (rightTop.x + width * 0.5), (int) (rightTop.y + height * 0.5));
        deleteDrawable.draw(canvas);

        // draw rotate icon at left-bottom.
//        width = (int) (rotateDrawable.getIntrinsicWidth() / scale);
//        height = (int) (rotateDrawable.getIntrinsicHeight() / scale);
        rotateDrawable.setBounds((int) (leftBottom.x - width * 0.5), (int) (leftBottom.y - height * 0.5), (int) (leftBottom.x + width * 0.5), (int) (leftBottom.y + height * 0.5));
        rotateDrawable.draw(canvas);

        // draw scale icon at right-bottom.
//        width = (int) (scaleDrawable.getIntrinsicWidth() / scale);
//        height = (int) (scaleDrawable.getIntrinsicHeight() / scale);
        scaleDrawable.setBounds((int) (rightBottom.x - width * 0.5), (int) (rightBottom.y - height * 0.5), (int) (rightBottom.x + width * 0.5), (int) (rightBottom.y + height * 0.5));
        scaleDrawable.draw(canvas);
    }

//    @Override
//    public void onCanvasScaleChanged(float scale, float oldScale) {
//
//    }

    public float centerX() {
        return center.x;
    }

    public float centerY() {
        return center.y;
    }

    public int getState() {
        return state;
    }

    public boolean isInRect(float x, float y) {
        return isInRect(new PointF(x, y));
    }

    public boolean isInRect(PointF p) {
        return cross(leftTop, leftBottom, p) * cross(rightBottom, rightTop, p) >= 0
                && cross(leftBottom, rightBottom, p) * cross(rightTop, leftTop, p) >= 0;
    }

    private float cross(PointF p1, PointF p2, PointF p3) {
        return (p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x) * (p2.y - p1.y);
    }

    public boolean startScaling() {
        if (NO_STATE == state) {
            this.state = SCALING_STATE;
            this.scale = 1;
        }
        return false;
    }

    public boolean scaling(float x, float y) {
        if (SCALING_STATE == state) {
            Vector2D width = new Vector2D(leftTop, rightTop);
            Vector2D height = new Vector2D(leftTop, leftBottom);

            float centerX = centerX();
            float centerY = centerY();

            Vector2D offset = new Vector2D(x - centerX, y - centerY);

            float widthOffset = width.dot(offset) / (float) Math.pow(width.length(), 2) * 2;
            float heightOffset = height.dot(offset) / (float) Math.pow(height.length(), 2) * 2;

            Log.d(TAG, "scaling: " + widthOffset + " " + heightOffset);

            float scale = Math.abs(widthOffset) > Math.abs(heightOffset) ? widthOffset : heightOffset;
            this.scale *= scale;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale, centerX, centerY);

            leftTop.x = centerX + (leftTop.x - centerX) * scale;
            leftTop.y = centerY + (leftTop.y - centerY) * scale;
            rightTop.x = centerX + (rightTop.x - centerX) * scale;
            rightTop.y = centerY + (rightTop.y - centerY) * scale;
            rightBottom.x = centerX + (rightBottom.x - centerX) * scale;
            rightBottom.y = centerY + (rightBottom.y - centerY) * scale;
            leftBottom.x = centerX + (leftBottom.x - centerX) * scale;
            leftBottom.y = centerY + (leftBottom.y - centerY) * scale;

            for (BasicElement element : chosenElements)
                element.onTransform(matrix);
            return true;
        }
        return false;
    }

    public boolean endScaling() {
        if (SCALING_STATE == state) {
            this.scale = 1;
            state = NO_STATE;
            return true;
        }
        return false;
    }

    public float getScale() {
        return this.scale;
    }

    public boolean startTranslating() {
        if (NO_STATE == state) {
            this.dx = 0;
            this.dy = 0;
            state = TRANSLATING_STATE;
            return true;
        }
        return false;
    }

    public boolean translating(float dx, float dy) {
        if (TRANSLATING_STATE == state) {
            Matrix matrix = new Matrix();
            matrix.postTranslate(dx, dy);
            this.dx += dx;
            this.dy += dy;

            leftTop.x += dx;
            leftTop.y += dy;

            rightTop.x += dx;
            rightTop.y += dy;

            rightBottom.x += dx;
            rightBottom.y += dy;

            leftBottom.x += dx;
            leftBottom.y += dy;

            center.x += dx;
            center.y += dy;

            for (BasicElement element : chosenElements)
                element.onTransform(matrix);
            return true;
        }
        return false;
    }

    public boolean endTranslating() {
        if (TRANSLATING_STATE == state) {
            this.dx = 0;
            this.dy = 0;
            state = NO_STATE;
            return true;
        }
        return false;
    }

    public Pair<Float, Float> getOffset() {
        return new Pair<Float, Float>(dx, dy);
    }

    public boolean startRotating() {
        if (NO_STATE == state) {
            this.rotate = 0;
            state = ROTATING_STATE;
        }
        return false;
    }

    public boolean rotating(PointF pointF) {
        if (ROTATING_STATE == state) {
            float centerX = centerX();
            float centerY = centerY();

            Vector2D oldVector2D = new Vector2D(leftBottom.x - centerX, leftBottom.y - centerY);
            Vector2D newVector2D = new Vector2D(pointF.x - centerX, pointF.y - centerY);
            float rotate = newVector2D.getEdgeRotateAngle() - oldVector2D.getEdgeRotateAngle();
            this.rotate += rotate;

            Vector2D vector2D;
            float angle, length;
            vector2D = new Vector2D(leftTop.x - centerX, leftTop.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            leftTop.x = (float) (centerX + length * Math.cos(angle));
            leftTop.y = (float) (centerY + length * Math.sin(angle));

            vector2D = new Vector2D(rightTop.x - centerX, rightTop.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            rightTop.x = (float) (centerX + length * Math.cos(angle));
            rightTop.y = (float) (centerY + length * Math.sin(angle));

            vector2D = new Vector2D(rightBottom.x - centerX, rightBottom.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            rightBottom.x = (float) (centerX + length * Math.cos(angle));
            rightBottom.y = (float) (centerY + length * Math.sin(angle));

            vector2D = new Vector2D(leftBottom.x - centerX, leftBottom.y - centerY);
            angle = vector2D.getEdgeRotateAngle() + rotate;
            length = vector2D.length();
            leftBottom.x = (float) (centerX + length * Math.cos(angle));
            leftBottom.y = (float) (centerY + length * Math.sin(angle));

            Matrix matrix = new Matrix();
            matrix.postRotate((float) Math.toDegrees(rotate), centerX, centerY);
            for (BasicElement onTransformListener : chosenElements)
                onTransformListener.onTransform(matrix);
            return true;
        }
        return false;
    }

    public boolean endRotating() {
        if (ROTATING_STATE == state) {
            this.rotate = 0;
            this.state = NO_STATE;
            return true;
        }
        return false;
    }

    public float getRotate() {
        return this.rotate;
    }

    public boolean startDeleting() {
        if (NO_STATE == state) {
            state = DELETING_STATE;
        }
        return false;
    }

    public boolean endDeleting() {
        if (DELETING_STATE == state) {
            onCancel();
            return true;
        }
        return false;
    }

//    private double getIncludedAngle(PointF start, PointF center, PointF end) {
//        return getIncludedAngle(start.x, start.y, center.x, center.y, end.x, end.y)
//    }
//
//    private float getIncludedAngle(float startX, float startY, float centerX, float centerY, float endX, float endY) {
//        double a = Math.sqrt(Math.pow(startX - centerX, 2) + Math.pow(startY - centerY, 2));
//        double b = Math.sqrt(Math.pow(endX - centerX, 2) + Math.pow(endY - centerY, 2));
//        double c = Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
//
//        float x1 = startX - centerY;
//        float y1 = startX - centerY;
//        float x2 = endX - centerY;
//        float y2 = endX - centerY;
//
//        if (x1 * y2 - x2 * y1 >= 0)
//            return (float) Math.acos((a * a + b * b - c * c) / (2 * a * b));
//        else
//            return (float) -Math.acos((a * a + b * b - c * c) / (2 * a * b));
//    }

    public boolean isPressScale(float x, float y) {
//        int width = (int) (scaleDrawable.getIntrinsicWidth() / canvasScale);
        int width = 100;
        return (Math.pow(rightBottom.x - x, 2) + Math.pow(rightBottom.y - y, 2) <= Math.pow(width * 0.5, 2));
    }

    public boolean isPressDelete(float x, float y) {
//        int width = (int) (deleteDrawable.getIntrinsicWidth() / canvasScale);
        int width = 100;
        return (Math.pow(rightTop.x - x, 2) + Math.pow(rightTop.y - y, 2) <= Math.pow(width * 0.5, 2));
    }

    public boolean isPressRotate(float x, float y) {
//        int width = (int) (scaleDrawable.getIntrinsicWidth() / canvasScale);
        int width = 100;
        return (Math.pow(leftBottom.x - x, 2) + Math.pow(leftBottom.y - y, 2) <= Math.pow(width * 0.5, 2));
    }

    public List<BasicElement> getChosenElements() {
        return chosenElements;
    }

}



